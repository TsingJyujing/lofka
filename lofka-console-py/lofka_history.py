#!/usr/bin/python3
# -*- coding:utf-8 -*-

import sys
import datetime
import traceback
from config import *
from console_util import *
from pymongo import MongoClient
from lofka_console import print_message
from console_util import message_formatter_raw
from bson import json_util

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
    print("{}\n\nQuerying...".format(LofkaColors.purple(LOFKA_BANNER)))
    print("Version 1.4 Author: TsingJyujing@163.com")

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

    aggregate_pipeline = [
        {
            "$match": query_info
        }
    ]

    if limit_info > 0:
        if direction:
            # 如果是摘取前面的日志
            aggregate_pipeline.append({
                "$sort": {
                    "timestamp": 1
                }
            })
        else:
            # 如果是摘取最后日志
            aggregate_pipeline.append({
                "$sort": {
                    "timestamp": -1
                }
            })

        aggregate_pipeline.append({
            "$limit": limit_info
        })

    # 顺序输出
    if not direction:
        aggregate_pipeline.append({
            "$sort": {
                "timestamp": 1
            }
        })

    conn = MongoClient("输入你的MongoDB的地址", 27017)
    db = conn.get_database("存放日志的数据库")
    db.authenticate("账号", "密码")
    coll = db.get_collection("Collection名称（一般是lofka）")
    print("Generating MongoDB querying language...")
    print("db.getCollection('lofka').aggregate({})".format(json_util.dumps(aggregate_pipeline, indent=2)))
    for doc in coll.aggregate(aggregate_pipeline):
        try:
            print_message(doc)
        except Exception as ex:
            print("Error while processing message:\n" + message_formatter_raw(doc), file=sys.stderr)
            print(str(ex), file=sys.stderr)
            print(traceback.format_exc(), file=sys.stderr)


if __name__ == '__main__':
    main()
