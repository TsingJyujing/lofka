#!/usr/bin/python3
# -*- coding:utf-8 -*-
import sys
import datetime
import time
import traceback
from config import *
from console_util import *
from websocket import create_connection, WebSocketConnectionClosedException


def format_datetime(timestamp_info: object) -> str:
    if type(timestamp_info) == datetime.datetime:
        logger_time = timestamp_info
    elif type(timestamp_info) == float or type(timestamp_info) == int:
        logger_time = datetime.datetime.fromtimestamp(timestamp_info / 1000.0)
    else:
        raise Exception("Invalid timestamp field:" + str(timestamp_info))
    time_formatted = "%s.%06d" % (logger_time.strftime("%Y-%m-%d %H:%M:%S"), logger_time.microsecond)
    return time_formatted


def message_formatter(log_data: dict) -> str:
    """
    消息格式化工具，格式化为你需要的格式
    :param log_data:
    :return:
    """
    # 格式化时间
    time_formatted = format_datetime(log_data["timestamp"])

    if "host" in log_data:
        host_formatted = host_info_format(log_data["host"])
    else:
        host_formatted = "Unknown host"

    logger_output = "{0} [{1}] [{2}] {3} [{6}://{5}]\t:{4}".format(
        time_formatted,
        LEVEL_COLOR[log_data["level"]](
            str_size_limit(log_data["level"], limit_ahead=2, limit_after=3, padding=True, shrink_str="-")),
        LofkaColors.purple(str_size_limit(log_data["thread"], limit_ahead=4, limit_after=10, padding=True)),
        LofkaColors.blue(str_size_limit(log_data["logger"], limit_ahead=4, limit_after=10, padding=True)),
        LofkaColors.cerulean(log_data["message"]),
        LofkaColors.cerulean(host_formatted),
        LofkaColors.yellow(log_data["app_name"])
    )
    if "throwable" in log_data:
        logger_output += "\n" + throwable_info_formatter(log_data["throwable"])
    return logger_output


def nginx_message_formatter(log_data: dict) -> str:
    time_formatted = format_datetime(log_data["timestamp"])
    http_status = log_data["message"]["status"]  # type: str
    if len(http_status) == 3:
        if http_status.startswith("2"):
            # 2xx means success
            http_status = LofkaColors.green(log_data["message"]["status"])
        elif http_status.startswith("3"):
            http_status = LofkaColors.yellow(log_data["message"]["status"])
        elif http_status.startswith("4"):
            http_status = LofkaColors.red(log_data["message"]["status"])
        elif http_status.startswith("5"):
            http_status = LofkaColors.purple(log_data["message"]["status"])
        else:
            http_status = LofkaColors.cerulean(log_data["message"]["status"])
    else:
        http_status = LofkaColors.cerulean(log_data["message"]["status"])
    logger_output = "{0} [{8}] [{1} {3} {2}] {6} --> {7} {4}\t{5}".format(
        time_formatted,
        LofkaColors.purple(
            str_size_limit(log_data["message"]["request_method"], limit_ahead=5, limit_after=5, padding=True)
        ),
        http_status,
        LofkaColors.white(log_data["message"]["server_protocol"]),
        LofkaColors.cerulean(log_data["message"]["host"]),
        LofkaColors.blue(log_data["message"]["uri"]),
        LofkaColors.cerulean(
            str_size_limit(log_data["message"]["upstream_addr"], limit_ahead=5, limit_after=10, padding=True)
        ),
        LofkaColors.blue(
            str_size_limit(log_data["message"]["remote_addr"], limit_ahead=5, limit_after=10, padding=True)
        ),
        LofkaColors.green(log_data["app_name"]),
    )
    return logger_output


def print_message(log_data: dict):
    if "type" in log_data:
        if log_data["type"].upper() == "NGINX":
            print(nginx_message_formatter(log_data))
        else:
            print(message_formatter(log_data))
    else:
        print(message_formatter(log_data))


script_template = """
%s
function filter(log){
%s
    return true;
}"""


def create_js_from_filter_map(filter_map: dict) -> str:
    i = 0
    const_js = ""
    run_js = ""
    for k, v in filter_map.items():
        i += 1
        const_js += "var set%d = {%s};\n" % (i, ",".join(["'%s':1" % x for x in v]))
        run_js += "    if(!(log%s in set%d)){return false}\n" % ("".join(["['%s']" % x for x in k.split(".")]), i)
    return script_template % (const_js, run_js)


# noinspection PyBroadException
def main():
    """
    主函数
    :return:
    """
    arg_map = ArgumentsMap(sys.argv)
    filter_map = {k: set(v.split(",")) for k, v in arg_map.query_all().items() if k not in {
        "ws_host",
        "ws_port",
        "print_raw"
    }}

    ws_host = arg_map.query_default("ws_host", "输入WebSocket的地址（IP/域名）")
    ws_port = arg_map.query_default("ws_port", "输入WebSocket的端口")
    print_raw = arg_map.query_default("print_raw", False)
    ws_address = "ws://{}:{}/lofka/websocket/logs".format(ws_host, ws_port)
    print("WebSocket connecting: {}".format(ws_address))
    ws = create_connection(ws_address)
    print("Connection established.")
    print("{}\n\nWaiting for messages.".format(LofkaColors.blue(LOFKA_BANNER)))

    js_script = create_js_from_filter_map(filter_map)
    ws.send(
        js_script
    )
    print("Filter generated: \n{}".format(js_script))
    print("Version 1.5 Author: TsingJyujing@163.com")
    log_data = {}
    try:
        while True:
            try:
                log_data = json.loads(ws.recv())
                # 防止过滤器未生效，使用本地过滤双重拦截
                if message_filter(log_data, filter_map):
                    if not print_raw:
                        print_message(log_data)
                    else:
                        print(json_util.dumps(log_data, indent=2))
            except WebSocketConnectionClosedException as wsc_ex:
                print("Web socket closed, reconnecting...")
                time.sleep(1.0)
                ws = create_connection(ws_address)
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
        ws.close()


if __name__ == '__main__':
    # process arguments
    main()
