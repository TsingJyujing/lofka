#!/usr/bin/python3
# coding=utf-8

"""
负责数据的后处理，数据库的管理和写入
"""
import datetime
import json
import logging
import time
import traceback
from abc import abstractmethod

from pykafka import KafkaClient
from pykafka.common import OffsetType
from pymongo import MongoClient, HASHED, ASCENDING, IndexModel

FORMAT = '%(asctime)-15s %(message)s'
logging.basicConfig(format=FORMAT, level=logging.INFO)
logger = logging.getLogger('mongo-writer')

timezone_offset = datetime.timedelta(hours=8)


class ILoggerSaver:
    """
    日志存储接口
    """

    @abstractmethod
    def save(self, json_data: dict): pass


class LoggerMongoDB(ILoggerSaver):
    expired_setting = {
        "TRACE": 5 * 60,
        "DEBUG": 10 * 60,
        "INFO": 24 * 60 * 60,
        "WARN": 31 * 24 * 60 * 60,
        "ERROR": 366 * 24 * 60 * 60,
        "FATAL": 732 * 24 * 60 * 60,
        # Default for 1 month
        "DEFAULT": 31 * 24 * 60 * 60,
        # Save Nginx log for half-year
        "NGINX": 190 * 24 * 60 * 60,
    }

    # noinspection PyBroadException
    @staticmethod
    def process_log(json_data: dict) -> object:

        # 处理传输过来的数字时间戳
        if "timestamp" in json_data:
            timestamp = json_data.get("timestamp") / 1000.0
            json_data["timestamp"] = datetime.datetime.fromtimestamp(timestamp)
        else:
            json_data["timestamp"] = datetime.datetime.now()
        # 增加写入时间戳
        json_data["write_timestamp"] = datetime.datetime.now()

        # 转换为大写
        if "level" in json_data:
            json_data["level"] = json_data["level"].upper()

        if "type" not in json_data:
            json_data["type"] = "NO_TYPE"
        else:
            json_data["type"] = json_data["type"].upper()

        try:
            expired_tick = json_data["expired_time"]
            assert type(expired_tick) == float, "expired not float"
            json_data["expired_time"] = datetime.datetime.fromtimestamp(expired_tick / 1000.0)
        except:
            # 确定过期时间
            if "level" in json_data:
                expired_delta = LoggerMongoDB.expired_setting[json_data["level"]]
            elif "type" in json_data:
                type_info = json_data["type"]
                if type_info.upper() == "NGINX":
                    expired_delta = LoggerMongoDB.expired_setting["NGINX"]
                else:
                    expired_delta = LoggerMongoDB.expired_setting["DEFAULT"]
            else:
                expired_delta = LoggerMongoDB.expired_setting["DEFAULT"]

            if expired_delta < 60 * 60:
                raise Exception("Save time less than 1 hour will not be written to db.")
            json_data["expired_time"] = datetime.datetime.now() + datetime.timedelta(seconds=expired_delta)

        message_raw = json_data["message"]

        # Trying to jsonize message if message in string format
        if type(message_raw) == str:
            try:
                json_data["message"] = json.loads(message_raw)
            except:
                pass

        return json_data

    def save(self, json_data: dict):
        """
        消息队列的数据保存到MongoDB
        :param json_data:
        :return:
        """
        return self.coll.insert_one(self.process_log(json_data))

    def __init__(self,
                 host: str = "127.0.0.1",
                 port: int = 27017,
                 db: str = "logger", coll: str = "lofka",
                 user: str = None, passwd: str = None
                 ):
        self.__conn = MongoClient(host, port)
        db = self.__conn.get_database(db)
        if user is not None and passwd is not None:
            db.authenticate(user, passwd)
            logger.info("Authenticated successfully.")
        # 判断表是否存在，不存在则创建，存在则直接返回
        if coll not in db.list_collection_names():
            logger.info("Initializing collection while non-exist.")
            lofka = db.get_collection("lofka")
            lofka.create_indexes([
                IndexModel([("timestamp", ASCENDING)]),
                IndexModel([("write_timestamp", ASCENDING)]),
                IndexModel([("expired_time", ASCENDING)], expireAfterSeconds=0),
                IndexModel([("app_name", HASHED)], sparse=True),
                IndexModel([("host.ip", HASHED)]),
                IndexModel([("level", HASHED)]),
                IndexModel([("kafka_info", ASCENDING)], unique=True),
                IndexModel([("logger", HASHED)]),
            ])
            lofka.create_index([
                ("message.uri", ASCENDING),
                ("message.host", ASCENDING),
            ], partialFilterExpression={"type": "NGINX"})
            lofka.create_index([
                ("message.request_time", ASCENDING),
            ], partialFilterExpression={"type": "NGINX"})
            lofka.create_index([
                ("message.remote_addr", HASHED),
            ], partialFilterExpression={"type": "NGINX"})
            lofka.create_index([
                ("message.upstream_response_time", ASCENDING),
            ], partialFilterExpression={"type": "NGINX"})
            lofka.create_index([
                ("message.status", HASHED),
            ], partialFilterExpression={"type": "NGINX"})
            lofka.create_index([
                ("message.http_user_agent", HASHED),
            ], partialFilterExpression={"type": "NGINX"})
        self.coll = db.get_collection(coll)

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.__conn.close()

    def __enter__(self):
        return self


# noinspection PyBroadException
def main():
    with open("config.json", "r") as fp:
        config = json.load(fp)

    with LoggerMongoDB(
            host=config["mongodb"]["host"],
            port=config["mongodb"]["port"],
            db=config["mongodb"]["db"],
            coll=config["mongodb"]["coll"],
            user=config["mongodb"]["user"],
            passwd=config["mongodb"]["passwd"]
    ) as lm:
        # 消息队列配置信息
        client = KafkaClient(hosts=",".join(config["kafka"]["brokers"]))
        topic_info = config["kafka"]["topic"]
        topic = client.topics[topic_info.encode()]
        balanced_consumer = topic.get_balanced_consumer(
            consumer_group=config["kafka"]["group_id"].format(topic_info, int(time.time() * 1000)).encode(),
            auto_commit_enable=True,  # 设置为Flase的时候不需要添加 consumer_group
            zookeeper_connect=",".join(config["kafka"]["zookeepers"]),  # 这里就是连接多个zk
            reset_offset_on_start=False,
            auto_offset_reset=OffsetType.LATEST
        )
        while True:
            msg = balanced_consumer.consume()

            message_detail = "%s:%d:%d: key=%s value=%s" % (
                topic_info,
                msg.partition_id,
                msg.offset,
                msg.partition_key,
                msg.value
            )

            logger.debug(message_detail)
            try:
                data = json.loads(msg.value.decode())
                data["kafka_info"] = {
                    "topic": topic_info,
                    "offset": msg.offset,
                    "partition": msg.partition_id
                }
                lm.save(data)

            except Exception as ex:
                logger.debug("Data: {}, Message: {}, Stack: {}".format(
                    message_detail,
                    str(ex),
                    traceback.format_exc()
                ))


if __name__ == '__main__':
    while True:
        try:
            main()
        except KeyboardInterrupt as kex:
            print("Interrupted by keyboard")
            break
        except Exception as main_ex:
            logger.fatal("Message: {}, Stack: {}".format(
                str(main_ex),
                traceback.format_exc()
            ))
            time.sleep(10)
