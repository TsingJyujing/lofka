#!/usr/bin/python3
# -*- coding:utf-8 -*-

import datetime
import os
import sys
import traceback

from pymongo import MongoClient

from config import *
from console_util import *
from console_util import message_formatter_raw
from lofka_console import print_message

DATETIME_FORMAT = "%Y%m%d%H%M%S"


def generate_message_filter(args: ArgumentsMap) -> dict:
    """
    创建过滤器
    :param args: 参数解析Map
    :return:
    """
    query_condition = {}
    for k, v in {a: b for a, b in args.query_all().items() if a not in HISTORY_FILTER_BLOCK_FIELDS}.items():
        if type(v) == bool:
            query_condition[k] = {
                "$ne": None
            }
        elif type(v) == str:
            filter_list = v.split(",")
            if len(filter_list) == 1:
                query_condition[k] = {
                    "$eq": v
                }
            else:
                query_condition[k] = {
                    "$in": filter_list
                }
    return query_condition


def main():
    with open(os.path.join(sys.path[0], "config.json"), "r") as fp:
        config = json.load(fp)["mongodb"]

    print("{}\n\nQuerying...".format(LofkaColors.purple(LOFKA_BANNER)))
    print("Version 1.7 Author: TsingJyujing@163.com")

    args = ArgumentsMap(sys.argv)
    query_info = generate_message_filter(args)

    timestamp_filter = {}

    if "start" in args.query_all():
        arg_start = args.query("start")
        timestamp_filter["$gte"] = datetime.datetime.strptime(
            arg_start,
            DATETIME_FORMAT[:(len(arg_start) - 2)]
        )
    if "end" in args.query_all():
        arg_end = args.query("end")
        timestamp_filter["$lte"] = datetime.datetime.strptime(
            arg_end,
            DATETIME_FORMAT[:(len(arg_end) - 2)]
        )
    if len(timestamp_filter) > 0:
        query_info["timestamp"] = timestamp_filter

    limit_info = int(args.query_default("limit", "0"))

    direction = args.query_default("head", False)

    conn = MongoClient(config["host"], int(config["port"]))  # 设定MongoDB的IP
    db = conn.get_database(config["db"])  # 设定数据库名称
    if "user" in config and "passwd" in config:
        db.authenticate(config["user"], config["passwd"])  # 鉴权
    coll = db.get_collection(config["coll"])  # 选定的Collection名称

    print("Generating MongoDB query language ...")

    if direction:
        # 取前面的数据
        sort_info = 1
    else:
        # 取后面的数据
        sort_info = -1

    cursor = coll.find(query_info).sort("timestamp", sort_info)

    query_statement = "db.getCollection('lofka').find({}).sort({})".format(
        LofkaColors.blue(json_util.dumps(query_info, indent=2)),
        LofkaColors.cerulean(json_util.dumps({
            "timestamp": sort_info
        }, indent=2))
    )

    if limit_info > 0:
        cursor = cursor.limit(limit_info)
        query_statement += ".limit(\n  {}\n)".format(LofkaColors.cerulean(str(limit_info)))

    if not direction:
        cursor = cursor.sort("timestamp", 1)
        query_statement += ".sort({})".format(
            LofkaColors.cerulean(LofkaColors.cerulean(json_util.dumps(
                {
                    "timestamp": 1
                },
                indent=2
            )))
        )

    print("MQL generated:\n{}".format(query_statement))
    for doc in cursor:
        try:
            print_message(doc)
        except Exception as ex:
            print("Error while processing message:\n" + message_formatter_raw(doc), file=sys.stderr)
            print(str(ex), file=sys.stderr)
            print(traceback.format_exc(), file=sys.stderr)


if __name__ == '__main__':
    main()
