# Logger Json 格式规范

## 简介

**任何符合LoggerJSON格式的数据都可以纳入MongoDB数据库存储**。

事实上，LoggerJSON的规定非常宽松，许多字段不需要严格遵守，但是如果不遵守这个规范可能无法直接使用官方（也就是我写的）控制台工具对日志进行输出。到时候你需要自己写输出到控制台的程序对日志进行输出。

当然你要是闲的蛋疼可以这个基础上自己定义一套格式，但是如果你真的有不一样的需求我建议你找我另外开辟一个Topic，你爱写啥写啥，爱怎么持久化怎么持久化。球球你就不要污染我的Topic了。

## 字段说明

|字段名称|数据类型|字段说明|是否必需|后处理说明|示例|
|-|-|-|-|-|-|
|level   |String   |日志等级，必需是TRACE, DEBUG, INFO, WARN, ERROR, FATAL这几个等级 |是|转为全大写|ERROR|
|message|String|日志信息|是|如果可变为JSON则变为JSON对象存储|"HTTP 404 Error"|
|timestamp|Long/Double|日志产生的时间，单位是毫秒，如果有纳秒的信息请放在小数位|否|如果不存在，以接收到日志的时间为准|1524046235357|
|expired_time|Long/Double|过期时间，逾期删除|否|如果不存在，timestamp+依据日志等级获取的保留时间为准|1524046235357|
|host|Map/Object|主机信息|否|如果不存在，则根据连接到服务器的信息自动生成|参见主机信息部分|
|routers   |List   |路由信息   |否   |每一级转发增加相应的IP信息   |参见路由信息部分   |
|app_name|String|应用名称|否|如果不存在，给定一个默认的app_name|"TestService"|
|throwable|Object|异常信息|否|无|参见异常部分|
|type|String|日志类型|否|保存时强制大写|一般标明日志的类型，如NGINX标明是"type"："NGINX"，这一字段在MONGO中做偏索引使用。|
|location|Object|日志产生所在的位置|否|无|参见日志位置部分|
|其它可扩展字段。。。|-|-|-|-|-|

### 应用名称（`app_name`字段）规范
`app_name`遵循类似文件夹层级的命名方式，唯一有区别的是生成的路径是反向的。
例如：
某MyCat节点的`app_name`是`mycat_node`，其向专门收集数据库日志的转发服务器发送日志之后，名称变为`database/mycat_node`。数据库的服务器向负责整个系统日志收集的服务器发送的时候变为：`hstsp/database/mycat_node`，最后一级Http服务器一般不设置app名称，最后就以`hstsp/database/mycat_node`入库。
如果没有专门收集数据库日志的转发服务器，那么可以直接设置`app_name`是`database/mycat_node`，以划分系统层级。


### 主机信息
主机信息的组成如下：
```json
{
    "name":"主机名称",
    "ip":"主机IP"
}
```

### 路由信息
路由信息的组成如下：
```js
[
    "第一层IP",
    "第二层IP",
    //...
    "最后一层IP"
]
```

某个日志在最终进入Kafka之前可能会经过多层转发，每经过一层转发，都会由转发服务器封装一层IP。

需要说明的是，主机信息最多允许16层，超过16层的日志会被直接废弃（说明有可能是产生了自激震荡），这个时候相关信息会输出到日志服务器的控制台。

### 日志位置
日志位置样例如下：
```js
{
    "method": "refresh",
    "line": 106,
    "filename": "ZkCoordinator.java",
    "class": "org.apache.storm.kafka.ZkCoordinator"
}
```

### 异常
一般的异常信息格式如下，其中的字段我相信不需要解释了，还需要在下解释的话自己退群吧。

```js
{
    "throwable":{
        "message":"异常产生的消息",
        "stack_trace":[//堆栈追踪列表
            {
                "class":"org.springframework.web.method.annotation.RequestParamMethodArgumentResolver",
                "filename":"RequestParamMethodArgumentResolver.java",
                "method":"handleMissingValue",
                "line":198
            },
            //...,这里可能有很多层
            {
                "class":"org.apache.tomcat.util.threads.TaskThread$WrappingRunnable",
                "filename":"TaskThread.java",
                "method":"run",
                "line":61
            },
            {
                "class":"java.lang.Thread",
                "filename":"Thread.java",
                "method":"run",
                "line":748
            }
        ]
    }
}
```

### 范例

下面给出一个典型的日志信息的范例，在传送日志信息的时候请尽可能的遵守这个规范。

```json
{
  "host": {
    "ip": "10.10.11.136",
    "name": "10.10.11.136"
  },
  "logger": "logback(slf4j)-test",
  "message": "Fun failed",
  "level": "ERROR",
  "timestamp": 1524708767509.0,
  "thread": "main",
  "throwable": {
    "stack_trace": [
      {
        "class": "com.cvnavi.loggerserver.MessageGenerator",
        "filename": "MessageGenerator.java",
        "method": "testFunL4",
        "line": 46
      },
      {
        "class": "com.cvnavi.loggerserver.MessageGenerator",
        "filename": "MessageGenerator.java",
        "method": "main",
        "line": 24
      }
    ],
    "message": "Exception Test"
  },
  "app_name": "data-warehouse-interface-mongo",
  "mdc": {}
}
```

如下是扩展的Nginx日志的示范：
```js
{
  "host": {
    "ip": "10.10.10.128",
    "name": "10.10.10.128"
  },
  "app_name": "cvnavi/cvtsp/nginx",
  "source": "/var/log/nginx/access.log",
  "timestamp": 1533522740808.0,
  "expired_time": 1549074740808.0,
  "routers": [
    "10.10.10.128"
  ],
  "message": {
    "scheme": "http",
    "host": "teamcity.cvtsp.com",
    "time_iso8601": "2018-08-06T10:32:20+08:00",
    "upstream_addr": "10.10.13.237:8111",
    "request_length": 2786.0,
    "remote_user": "-",
    "request_time": 0.004,
    "http_user_agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36",
    "request_method": "POST",
    "bytes_sent": 1785.0,
    "remote_addr": "10.10.10.1",
    "http_x_forwarded_for": "-",
    "body_bytes_sent": 1593.0,
    "args": [],
    "upstream_response_time": 0.004,
    "server_protocol": "HTTP/1.1",
    "msec": 1533522740.808,
    "status": "200",
    "uri": "/subscriptions.html",
    "http_referer": "http://teamcity.cvtsp.com/overview.html",
    "pipe": "."
  },
  "type": "NGINX"
}

```
