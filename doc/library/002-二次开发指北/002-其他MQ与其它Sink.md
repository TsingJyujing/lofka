# 其他MQ与其它Sink

## 使用其他MQ
不推荐使用Kafka以外的MQ，因为很多工具都是基于Kafka去构建的，例如日志的WebSocket推送等等。

如果要使用其它类型的MQ，继承`com.github.tsingjyujing.lofka.server.queue.IMessageQueue`，实现推送方法，并且在`MessageQueueCluster`中增加初始化你的消息队列写入类即可。

如果一定要使用其它类型的MQ，建议保留一个Kafka给lofka-console与WebSocket使用。


## 持久化到其他Sink
除了持久化到MongoDB，还支持其他类型的持久化，只需要从消息队列（默认推荐使用Kafka）中逐个读出对象并且插入你要的数据库即可。
