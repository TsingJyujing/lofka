# 其他MQ与其它Sink

不推荐使用Kafka以外的MQ，因为很多工具都是基于Kafka去构建的，例如日志的WebSocket推送等等。
但是可以持久化到其他的Sink，只需要实现相关代码，重新编译配置即可。

目前支持持久化到MongoDB和本地文件。

计划支持 ElasticSearch/SQL(MySQL/PostgreSQL)。

## 持久化到其他Sink

除了持久化到MongoDB，还支持其他类型的持久化，只需要从消息队列（默认推荐使用Kafka）中逐个读出对象并且插入你要的数据库即可。

不过现在你有更方便的方式去实现这些。

首先实现`IBatchLoggerProcessor`接口，在这里你需要编写`void processLoggers(Iterable<Document> logs);`方法来指定程序如何处理你的数据，一些处理的参考可以在`DocumentUtil`工具类中找到。

在初始化的时候可能有一些参数需要配置，例如数据库的连接信息，你在初始化的时候可以得到一个`Map<String,String>`或者是`Properties`对象，所以初始化函数一般是`public 类名(Properties properties){...}`。

之后在`ConfigLoader`的`processorFactory`中增加一段case，并且利用`ProcessorInfo`中的信息初始化你的持久化类。

最后，要让你的代码生效，只需要在`conf/lofka-persistence.json`文件中的`processors`对象中增加一对key-value来自定义你的配置，其中key是名称，可以随意给定，只要不冲突即可。
value的定义如下例子所示：

```js
//标准的JSON不支持注释，使用的时候请注意
{
    "processorType": "mongodb",//你在case段中定义的名称
    "config": {//使用JSON描述的Properties信息
        "mongodb.collection": "lofka",
        "mongodb.database": "logger",
        "mongodb.deprecate.time": "3600000",
        "mongodb.expired.setting.DEBUG": "600000",
        "mongodb.expired.setting.DEFAULT": "2678400000",
        "mongodb.expired.setting.ERROR": "31622400000",
        "mongodb.expired.setting.FATAL": "63244800000",
        "mongodb.expired.setting.INFO": "86400000",
        "mongodb.expired.setting.NGINX": "16416000000",
        "mongodb.expired.setting.TRACE": "300000",
        "mongodb.expired.setting.WARN": "2678400000",
        "mongodb.servers": "127.0.0.1:27017"
    }
}
```

## 其他MQ

### 推送到其他MQ

需要修改的项目是 lofka-server，继承`com.github.tsingjyujing.lofka.server.queue.IMessageQueue`，实现推送方法，并且在`MessageQueueCluster`中增加初始化你的消息队列写入类即可。

如果一定要使用其它类型的MQ，建议保留一个资源有限的Kafka给lofka-console与WebSocket使用。

### 从其他MQ接收数据做持久化

这里需要修改的项目是 lofka-persistence，首先实现`com.github.tsingjyujing.lofka.persistence.basic.ILogReceiverProcessable`接口（或者`ILogReceiver`也可以）。

你实现的类可能需要一些外部参数的初始化，同时也会需要知道从这个MQ接收的数据究竟要做什么操作。初始化的时候你可以使用一个`Properties`对象和一个`Iterable<IBatchLoggerProcessor>`对象。

随后你把初始化的过程写在`ConfigLoader`的`sourceFactory`中，增加一个case段，这样就能通过配置加载你写的代码。

配置文件参考如下：

```js
//标准的JSON不支持注释，使用的时候请注意
{
    "sourceType": "kafka",//你在case段中定义的名称
    "config": {//使用JSON描述的Properties信息
        "auto.commit.interval.ms": "99999999",
        "auto.offset.reset": "latest",
        "bootstrap.servers": "10.10.11.75:9092",
        "enable.auto.commit": "true",
        "group.id": "logger-json-persistence-consumer",
        "kafka.topic": "logger-json",
        "key.deserializer": "org.apache.kafka.common.serialization.IntegerDeserializer",
        "value.deserializer": "org.apache.kafka.common.serialization.StringDeserializer"
    },
    "processors": {//这里配置所有需要的操作
        "mongodb-saver-0": {
            "processorType": "mongodb",
            "config": {
                "processorType": "mongodb",
                "config": {
                    "mongodb.collection": "lofka",
                    "mongodb.database": "logger",
                    "mongodb.deprecate.time": "3600000",
                    "mongodb.expired.setting.DEBUG": "600000",
                    "mongodb.expired.setting.DEFAULT": "2678400000",
                    "mongodb.expired.setting.ERROR": "31622400000",
                    "mongodb.expired.setting.FATAL": "63244800000",
                    "mongodb.expired.setting.INFO": "86400000",
                    "mongodb.expired.setting.NGINX": "16416000000",
                    "mongodb.expired.setting.TRACE": "300000",
                    "mongodb.expired.setting.WARN": "2678400000",
                    "mongodb.servers": "127.0.0.1:27017"
                }
            }
        }
    }
}
```

其中`Iterable<IBatchLoggerProcessor>`对象由配置文件的`processors`段初始化。
