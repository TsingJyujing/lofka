#!/usr/bin/python3
# coding=utf-8

from console_util import LofkaColors

LOFKA_BANNER = """
    __           ____ __             ______                            __
   / /   ____   / __// /__ ____ _   / ____/____   ____   _____ ____   / /___
  / /   / __ \\ / /_ / //_// __ `/  / /    / __ \\ / __ \\ / ___// __ \\ / // _ \\
 / /___/ /_/ // __// ,<  / /_/ /  / /___ / /_/ // / / /(__  )/ /_/ // //  __/
/_____/\\____//_/  /_/|_| \\__,_/   \\____/ \\____//_/ /_//____/ \\____//_/ \\___/
"""

# 日志等级数值
LEVEL_VALUE = {
    "TRACE": 0,
    "DEBUG": 1,
    "INFO": 2,
    "WARN": 3,
    "ERROR": 4,
    "FATAL": 5
}

# 颜色对应的等级配置
LEVEL_COLOR = {
    "TRACE": LofkaColors.cerulean,
    "DEBUG": LofkaColors.blue,
    "INFO": LofkaColors.green,
    "WARN": LofkaColors.yellow,
    "ERROR": LofkaColors.red,
    "FATAL": LofkaColors.red,
}

# 不允许在MongoDB中过滤的字段（一般是默认参数）
HISTORY_FILTER_BLOCK_FIELDS = {
    "start", "end", "limit", "head"
}
