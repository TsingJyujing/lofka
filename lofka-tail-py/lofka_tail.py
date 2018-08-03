#!/usr/bin/python
# coding=utf-8

import os
import abc
import sys
import time
import json
import datetime
import traceback

try:
    from urllib import unquote
except:
    from urllib.parse import unquote
import requests

banner = """
 _              __  _            _____       _  _ 
| |            / _|| |          |_   _|     (_)| |
| |      ___  | |_ | | __ __ _    | |  __ _  _ | |
| |     / _ \ |  _|| |/ // _` |   | | / _` || || |
| |____| (_) || |  |   <| (_| |   | || (_| || || |
\_____/ \___/ |_|  |_|\_\\\\__,_|   \_/ \__,_||_||_|
                                                  
"""

help_doc = """----------LOFKA TAIL FILE READER----------
options    type     usage                          default
--file     string   which file to monitor          [no default]
--target   string   url of logger server           http://logger.cvtsp.com/
--period   float    the period to scan the file    1.0
--type     list[]   which processors will be used  common
--app_name string   application name               lofka_tail
--append   string   a json file name to read       [no default]
"""


class Tail(object):
    """
    Represents a tail command.
    """

    def __init__(self, tailed_file):
        """
        Initiate a Tail instance.
            Check for file validity, assigns callback function to standard out.
        :param tailed_file: File to be followed
        """
        self.check_file_validity(tailed_file)
        self.tailed_file = tailed_file
        self.callback = sys.stdout.write

    def follow(self, s=1.0):
        """
        Do a tail follow. If a callback function is registered it is called with every new line.
        Else printed to standard out.
        :param s: Number of seconds to wait between each iteration; Defaults to 1.
        :return:
        """

        with open(self.tailed_file) as file_:
            # Go to the end of file
            file_.seek(0, 2)
            while True:
                curr_position = file_.tell()
                line = file_.readline()
                if not line:
                    file_.seek(curr_position)
                    time.sleep(s)
                else:
                    self.callback(line)

    def register_callback(self, func):
        """
        Overrides default callback function to provided function.
        :param func:
        :return:
        """
        self.callback = func

    @staticmethod
    def check_file_validity(file_):
        """
        Check whether the a given file exists, readable and is a file
        :param file_:
        :return:
        """
        if not os.access(file_, os.F_OK):
            raise TailError("File '%s' does not exist" % file_)
        if not os.access(file_, os.R_OK):
            raise TailError("File '%s' not readable" % file_)
        if os.path.isdir(file_):
            raise TailError("File '%s' is a directory" % file_)


class TailError(Exception):
    def __init__(self, msg):
        self.message = msg

    def __str__(self):
        return self.message


class ArgumentsMap:
    """
    参数解析为Map
    """

    def __init__(self, args):
        """
        :type args: List[str]
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

    def query(self, key):
        if key not in self.__data:
            raise Exception("key %s not in args" % key)
        return self.__data[key]

    def query_default(self, key, default_value):
        if key in self.__data:
            return self.__data[key]
        else:
            return default_value

    def query_all(self):
        return self.__data


class LogProcessor:
    """
    Log process frame work
    """

    def __init__(self):
        pass

    @abc.abstractmethod
    def process_text(self, text):
        """
        Process log here
        :param text: text file
        :return: Dict[str,object] data will append to logger json
        """
        pass


class CommonLogProcessor(LogProcessor):
    """
    Common log processor
    """

    def process_text(self, text):
        return {
            "message": text,
            "type": "text"
        }


class ArgumentsCommonLogProcessor(LogProcessor):
    """
    Arguments Common log processor
    """

    def process_text(self, text):
        return {
            "message": ArgumentsMap(text.split("\t\t")).query_all(),
            "type": "args"
        }


class NginxLogProcessor(LogProcessor):
    """
    Nginx log processor
    """

    integer_fields = set(["body_bytes_sent", "request_length", "bytes_sent"])

    def process_text(self, text):
        message = ArgumentsMap(text.split("\t\t")).query_all()
        return_data = {"type": "nginx"}
        # cover timestamp field if the time in message
        try:
            if "msec" in message:
                unix_timestamp = float(message["msec"])
                message["msec"] = unix_timestamp
                return_data["timestamp"] = unix_timestamp * 1000
            elif "time_iso8601" in message:
                return_data["timestamp"] = time.mktime(
                    datetime.datetime.strptime(message["time_iso8601"][:-6], "%Y-%m-%dT%H:%M:%S").timetuple()
                ) * 1000.0
            # format the _time field, they're all described with float (and in format of seconds)
            for k, v in message.items():
                if k.endswith("_time"):
                    try:
                        message[k] = float(v)
                    except:
                        pass

            # Fields should convert to int
            message = NginxLogProcessor.integer_convert(message, NginxLogProcessor.integer_fields)

            if "request_uri" in message:
                message["request_uri"] = NginxLogProcessor.decode_uri(message["request_uri"])

            if "args" in message:
                message["args"] = NginxLogProcessor.decode_args(message["args"])
        except:
            print("Error while processing nginx log:")
            print(traceback.format_exc())
        return_data["message"] = message
        return return_data

    @staticmethod
    def float_convert(data_map, float_fields):
        return NginxLogProcessor.data_convert(data_map, float_fields, lambda s: float(s))

    @staticmethod
    def integer_convert(data_map, int_fields):
        return NginxLogProcessor.data_convert(data_map, int_fields, lambda s: int(s))

    @staticmethod
    def data_convert(data_map, convert_fields, convert_method):
        for k, v in data_map.items():
            if k in convert_fields:
                data_map[k] = convert_method(v)
        return data_map

    @staticmethod
    def decode_args(args):
        if args == "-":
            return []
        arg_list = []
        for pair_str in args.split("&"):
            kv = pair_str.split("=")
            if len(kv) == 2:
                arg_list.append({
                    "key": kv[0],
                    "value": unquote(kv[1])
                })
            else:
                arg_list.append({
                    "key": pair_str,
                    "value": ""
                })
        return arg_list

    @staticmethod
    def decode_uri(uri):
        ss = uri.split("?")
        if len(ss) == 1:
            return {
                "path": ss[0],
                "args": []
            }
        else:
            return {
                "path": ss[0],
                "args": NginxLogProcessor.decode_args(ss[1])
            }


def log_processor_factory(processor_type):
    """
    Factory method to create processors
    :type processor_type: str
    :param processor_type:
    :return:
    """
    if processor_type == "common":
        return CommonLogProcessor()
    elif processor_type == "nginx":
        return NginxLogProcessor()
    elif processor_type == "arg_common":
        return ArgumentsCommonLogProcessor()
    else:
        raise Exception("Unrecognized processor type: %s" % processor_type)


def report_log(target, record_object):
    """
    Post log to target address
    :type target: str
    :type record_object: object
    :param target: target address
    :param record_object: object which jsonizable
    :return:
    """
    url = target + "lofka/service/push"
    return requests.post(url, data=json.dumps(record_object).encode())


def message_filter_function(data):
    # type: (dict) -> bool
    """
    Message filter
    :param data: LoggerJSON message body
    :return:
    """
    try:
        return data["message"]["host"] != "logger.cvtsp.com"
    except:
        return True


def main():
    arg_map = ArgumentsMap(sys.argv)
    report_address = arg_map.query_default("target", "http://logger.cvtsp.com/")
    file_name = arg_map.query("file")
    delta_time = float(arg_map.query_default("period", "1.0"))
    expired_delta_mills = float(arg_map.query_default("expired", "180.0")) * 24 * 60 * 60 * 1000
    app_name = arg_map.query_default("app_name", "lofka_tail")
    processors = [log_processor_factory(x) for x in arg_map.query_default("type", "common").split(",")]
    try:
        append_file = arg_map.query("append")
        with open(append_file, "rb") as fp:
            init_map = json.load(fp)
    except:
        init_map = {}
    init_map["app_name"] = app_name
    init_map["source"] = file_name

    use_console = not report_address.startswith("http")
    if use_console:
        print("WARNING: invalid target input (not start with http), use console in debug mode.")

    def processor(line_text):
        for processor_unit in processors:
            try:
                data_map = init_map
                data_map["timestamp"] = time.time() * 1000
                for k, v in processor_unit.process_text(line_text).items():
                    data_map[k] = v
                data_map["expired_time"] = data_map["timestamp"] + expired_delta_mills
                if message_filter_function(data_map):
                    if use_console:
                        print(report_address)
                        print(json.dumps(data_map, indent=2))
                    else:
                        report_log(report_address, data_map)
            except:
                print(traceback.format_exc())

    tail_file = Tail(file_name)
    tail_file.register_callback(processor)
    tail_file.follow(delta_time)


if __name__ == "__main__":
    print(banner)
    while True:
        try:
            assert not ArgumentsMap(sys.argv).query_default("help", False)
            main()
        except KeyboardInterrupt as kex:
            print("Interrupted by keyboard")
            break
        except Exception as ex:
            print(traceback.format_exc())
            print(help_doc)
