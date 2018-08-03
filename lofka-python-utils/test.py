# coding=utf8
"""
方便调试使用
"""

from lofka import LofkaHandler,LofkaAsyncHandler
import logging
import traceback

handler = LofkaAsyncHandler()
logger = logging.getLogger('test')
logger.addHandler(handler)


def __debug_method():
    try:
        raise Exception("TestException")
    except Exception as ex:
        traceback.format_exc()
        logger.exception("ErrorTitle")


if __name__ == "__main__":
    __debug_method()
