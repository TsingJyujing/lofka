#!/usr/bin/python3
# coding=utf-8
from bson import json_util
import json
from typing import List, Dict


class LofkaColors:
    """
    控制颜色的显示，调用Lofka.颜色(消息)即可将消息格式化为相应的颜色
    """

    def __init__(self):
        pass

    @staticmethod
    def __mark_color_all(message: str, foreground: int, background: int):
        return "\033[1;3%d;4%dm%s\033[0m" % (foreground, background, message)

    @staticmethod
    def __mark_color(message: str, foreground: int):
        return "\033[1;3%dm%s\033[0m" % (foreground, message)

    @staticmethod
    def black(message: str):
        return LofkaColors.__mark_color(message, 0)

    @staticmethod
    def red(message: str):
        return LofkaColors.__mark_color(message, 1)

    @staticmethod
    def green(message: str):
        return LofkaColors.__mark_color(message, 2)

    @staticmethod
    def yellow(message: str):
        return LofkaColors.__mark_color(message, 3)

    @staticmethod
    def blue(message: str):
        return LofkaColors.__mark_color(message, 4)

    @staticmethod
    def purple(message: str):
        return LofkaColors.__mark_color(message, 5)

    @staticmethod
    def cerulean(message: str):
        return LofkaColors.__mark_color(message, 6)

    @staticmethod
    def white(message: str):
        return LofkaColors.__mark_color(message, 7)


class ArgumentsMap:
    """
    参数解析为Map
    """

    def __init__(self, args: List[str]):
        """
        初始化参数解析
        :param args: sys.argv
        """
        self.__data = dict()
        for i in range(len(args) - 1):
            if args[i].startswith("--"):
                if args[i + 1].startswith("--"):
                    key = args[i][2:]
                    self.__data[key] = True
                else:
                    key = args[i][2:]
                    value = args[i + 1]
                    self.__data[key] = value
        if args[-1].startswith("--"):
            key = args[-1][2:]
            self.__data[key] = True

    def query(self, key: str):
        return self.__data[key]

    def query_default(self, key: str, default_value):
        if key in self.__data:
            return self.__data[key]
        else:
            return default_value

    def query_all(self):
        return self.__data


def get_or_default(obj, key, default_value):
    try:
        return obj.get(key)
    except:
        return default_value


def str_size_limit(
        text: str,
        limit_ahead: int = 3,
        limit_after: int = 7,
        padding: bool = False,
        shrink_str: str = "..."
) -> str:
    """
    字符串长度限制缩减
    :param text: 需要处理的文本
    :param limit_ahead: 前端长度限制
    :param limit_after: 后端长度限制
    :param padding: 对于长度不足的是否留空格
    :param shrink_str: 缩减表示省略使用的字符串
    :return: 处理后的文本
    """
    max_size_limit = limit_ahead + limit_after + len(shrink_str)
    raw_size = len(text)
    if raw_size == max_size_limit:
        return text
    elif raw_size < max_size_limit:
        if padding:
            padding_size = max_size_limit - raw_size
            return "".join((" " for _ in range(padding_size))) + text
        else:
            return text
    else:
        return text[:limit_ahead] + shrink_str + text[-limit_after:]


def get_linked_key_in_dict(x: dict, k: str):
    """
    从字典中链式取值的算法
    :param x: 字典
    :param k: 键值
    :return:
    """
    if k.find(".") < 0:
        return x[k]
    else:
        splitted_key = k.split(".", 1)
        return get_linked_key_in_dict(x[splitted_key[0]], splitted_key[1])


def host_info_format(host_info: dict):
    """
    格式化机器信息，如果主机名和IP一样就只显示IP，否则在括号中显示主机名称
    :param host_info:
    :return:
    """
    if host_info["name"] == host_info["ip"]:
        return host_info["ip"]
    else:
        return "%s(%s)" % (host_info["ip"], host_info["name"])


def message_formatter_raw(log_data: dict) -> str:
    """
    消息日志直接输出，当格式化出错的时候就这样输出
    :param log_data:
    :return:
    """
    return json.dumps(log_data, indent=2, default=json_util.default)


def throwable_info_formatter(throwable: dict) -> str:
    """
    格式化可抛对象
    :param throwable:
    :return:
    """
    stack_info = "\n".join(
        "    at {0} of {1}\t({2}#{3})".format(
            LofkaColors.red(get_or_default(stack, "method", "NO METHOD")),
            LofkaColors.red(get_or_default(stack, "class", "NO CLASS")),
            LofkaColors.cerulean(get_or_default(stack, "filename", "NO FILE")),
            LofkaColors.blue(str(int(get_or_default(stack, "line", "-1"))))
        ) for stack in throwable["stack_trace"]
    )

    if "message" in throwable:
        exception_msg = throwable["message"]
    else:
        exception_msg = "NO MESSAGE"
    return "  Exception message: {}\n{}".format(LofkaColors.red(exception_msg), stack_info)


# noinspection PyBroadException
def message_filter(log_data: dict, filter_map: Dict[str, set]) -> bool:
    """
    消息过滤器，可以对来源、等级、IP等进行复杂过滤
    :param filter_map: 过滤器
    :param log_data: 日志数据
    :return: True则显示，False则不显示
    """
    try:
        for k, s in filter_map.items():
            if get_linked_key_in_dict(log_data, k) not in s:
                return False
        else:
            return True
    except Exception as _:
        return False
