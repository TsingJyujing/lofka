#!/usr/bin/python3
# -*- coding:utf-8 -*-

from pykafka import KafkaClient
from pykafka.common import OffsetType

from lofka_console import *


def main():
    """
    主函数
    :return:
    """
    with open("config.json", "r") as fp:
        config = json.load(fp)
    arg_map = ArgumentsMap(sys.argv)
    filter_map = {k: set(v.split(",")) for k, v in arg_map.query_all().items() if k not in {
        "topic",
        "bootstrap_servers",
        "zookeeper_servers",
        "print_raw"
    }}
    print_raw = arg_map.query_default("print_raw", False)
    topic_info = arg_map.query_default("topic", config["topic"])  # 设置你的Topic名称
    kafka_servers = arg_map.query_default(
        "bootstrap_servers",
        ",".join(config["brokers"])
    )  # 设置你的 kafka bootstrap_servers 名称
    zookeeper_servers = arg_map.query_default(
        "zookeeper_servers",
        ",".join(config["zookeepers"])
    )  # 设置ZK地址
    print(
        "Kafka config: servers={} zookeeper={} topic={}".format(
            kafka_servers.split(","),
            zookeeper_servers.split(","),
            topic_info
        )
    )
    client = KafkaClient(hosts=kafka_servers)
    topic = client.topics[topic_info.encode()]
    balanced_consumer = topic.get_balanced_consumer(
        consumer_group="console-{}-{}".format(topic_info, int(time.time() * 1000)).encode(),
        auto_commit_enable=True,  # 设置为False的时候不需要添加 consumer_group
        zookeeper_connect=zookeeper_servers,  # 这里就是连接多个zk
        reset_offset_on_start=False,
        auto_offset_reset=OffsetType.LATEST
    )
    print("{}\n\nConnected, waiting for messages.".format(LofkaColors.blue(LOFKA_BANNER)))
    print("Version 1.7 Author: TsingJyujing@163.com")
    try:
        while True:
            try:
                msg = balanced_consumer.consume()
                log_data = json.loads(msg.value.decode())
                if message_filter(log_data, filter_map):
                    if not print_raw:
                        print_message(log_data)
                    else:
                        print(json_util.dumps(log_data, indent=2))
            except Exception as message_ex:
                print("Error while processing message:\n" + message_formatter_raw(log_data), file=sys.stderr)
                print(str(message_ex), file=sys.stderr)
                print(traceback.format_exc(), file=sys.stderr)
    except KeyboardInterrupt as _:
        print("Got exit signal, exiting.")
    except Exception as ex:
        print("Error:\n" + str(ex), file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
    finally:
        # 料理后事
        balanced_consumer.stop()


if __name__ == '__main__':
    # process arguments
    main()
