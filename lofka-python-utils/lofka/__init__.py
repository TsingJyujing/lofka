import linecache
from threading import Thread
import sys
from logging import Handler, Formatter
from logging import CRITICAL, ERROR, WARNING, INFO, DEBUG, NOTSET
import time

try:
    from Queue import Queue
except:
    from queue import Queue
import requests
import json

_levelToName = {
    CRITICAL: 'FATAL',
    ERROR: 'ERROR',
    WARNING: 'WARN',
    INFO: 'INFO',
    DEBUG: 'DEBUG',
    NOTSET: 'TRACE',
}


class LoggerJsonFormatter(Formatter):
    """
    Format record in LoggerJson format
    """

    def format(self, record):
        """Formats LogRecord into python dictionary."""
        # Standard document
        document = {
            'timestamp': time.time() * 1000.0,
            'level': _levelToName[record.levelno],
            'thread': record.threadName,
            'thread_id': record.thread,
            'message': record.getMessage(),
            'logger': record.name,
            'location': {
                'filename': record.pathname,
                'class': record.module,
                'method': record.funcName,
                'line': record.lineno
            }
        }
        # Standard document decorated with exception info
        if record.exc_info is not None:
            document.update({
                'throwable': {
                    'message': str(record.exc_info[1]),
                    'stack_trace': [
                        {
                            "line": stack[1],
                            "filename": stack[0],
                            "method": stack[2],
                            "line_code": stack[3]
                        }
                        for stack in LoggerJsonFormatter.extract_tb(record.exc_info[2])
                    ]
                }
            })
        return document

    @staticmethod
    def extract_tb(tb, limit=None):
        """Return list of up to limit pre-processed entries from traceback.

        This is useful for alternate formatting of stack traces.  If
        'limit' is omitted or None, all entries are extracted.  A
        pre-processed stack trace entry is a quadruple (filename, line
        number, function name, text) representing the information that is
        usually printed for a stack trace.  The text is a string with
        leading and trailing whitespace stripped; if the source is not
        available it is None.
        """
        if limit is None:
            if hasattr(sys, 'tracebacklimit'):
                limit = sys.tracebacklimit
        list = []
        n = 0
        while tb is not None and (limit is None or n < limit):
            f = tb.tb_frame
            lineno = tb.tb_lineno
            co = f.f_code
            filename = co.co_filename
            name = co.co_name
            linecache.checkcache(filename)
            line = linecache.getline(filename, lineno, f.f_globals)
            if line:
                line = line.strip()
            else:
                line = None
            list.append((filename, lineno, name, line))
            tb = tb.tb_next
            n = n + 1
        return list


class LofkaHandler(Handler):
    """
    Log handler which sending
    """

    def __init__(self, target_url, app_name="default_python_application"):
        super(LofkaHandler, self).__init__()
        try:
            with open("lofka.json", "r") as fp:
                obj = json.load(fp)
                target_url = obj['target_url']
                app_name = obj['app_name']
        except:
            pass
        self.target_url = target_url + "lofka/service/push"
        self.app_name = app_name
        self.formatter = LoggerJsonFormatter()

    def emit(self, record):
        """
        Commit record to server
        :param record:
        :return:
        """
        record_object = self.formatter.format(record)
        record_object["app_name"] = self.app_name
        requests.post(self.target_url, data=json.dumps(record_object))


class LofkaAsyncHandler(Handler):
    """
    Log handler which sending
    """

    def __init__(self,
                 target_url,
                 app_name="default_python_application",
                 interval=1000,
                 max_buffer_size=1000
                 ):
        super(LofkaAsyncHandler, self).__init__()
        try:
            with open("lofka.json", "r") as fp:
                obj = json.load(fp)
                target_url = obj['target_url']
                app_name = obj['app_name']
                interval = int(obj['interval'])
                max_buffer_size = int(obj['max_buffer_size'])
        except:
            pass
        self.target_url = target_url + "lofka/service/push/batch"
        self.app_name = app_name
        self.formatter = LoggerJsonFormatter()
        self.message_queue = Queue(int(max_buffer_size * 1.3))  # type: Queue
        self.max_buffer_size = max_buffer_size

        def push_data_periodically():
            while True:
                if self.message_queue.qsize() > 0:
                    self.__submit_batch(list(self.message_queue.queue))
                    self.message_queue.queue.clear()
                else:
                    time.sleep(interval / 1000.0)

        Thread(target=push_data_periodically).start()

    def __submit_batch(self, data):
        """
        Submit messages
        :type data: list
        :param data: messages
        :return:
        """
        requests.post(self.target_url, data=json.dumps(data))

    def emit(self, record):
        """
        Commit record to server
        :param record:
        :return:
        """
        record_object = self.formatter.format(record)
        record_object["app_name"] = self.app_name
        self.message_queue.put(record_object, timeout=1)
        if self.message_queue.qsize() > self.max_buffer_size:
            self.__submit_batch(list(self.message_queue.queue))
            self.message_queue.queue.clear()
