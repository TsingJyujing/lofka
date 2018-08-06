# Http推送接口

POST参考Java代码：见`com.github.tsingjyujing.lofka.util.NetUtil`类中相关的代码。

接口信息：

|接口简介|接口相对地址|支持方法|调用说明|返回值格式|
|-|-|-|-|-|
|单条日志提交（无压缩）   |/lofka/service/push   |POST|POST Body中放置未压缩的LoggerJSON序列化后的字符串   |JSON通用返回格式（status=200代表正常）|
|多条日志提交（无压缩）   |/lofka/service/push/batch   |POST|POST Body中放置未压缩的，多个LoggerJSON组成的List序列化以后字符串   |JSON通用返回格式（status=200代表正常）|
|单条日志提交（压缩）   |/lofka/service/push/zip   |POST|POST Body中放置GZip压缩后的LoggerJSON序列化后的字符串   |JSON通用返回格式（status=200代表正常）|
|多条日志提交（压缩）   |/lofka/service/push/batch/zip   |POST|POST Body中放置GZip压缩后的，多个LoggerJSON组成的List序列化以后字符串   |JSON通用返回格式（status=200代表正常）|
|Socket Server 状态监控   |/lofka/socket/servers/monitor   |POST/GET|不需要参数   |JSON格式的数据|
|首页   |/   |POST/GET|不需要参数   |HTML 页面|
|WebSocket日志流推送   |/lofka/websocket/logs   |WebSocket |不需要参数   |每一条数据都是一条日志|
|~~非安全日志推送~~   |/lofka/service/unsafe_push   |POST |POST Body中放置任意想推送到Kafka的字符串   |JSON通用返回格式（status=200代表正常）|
|~~Kafka推送代理API~~   |/lofka/kafka/api/push   |POST |POST Body中放置JSON对象，`topic`字段是kafka的topic，`message`字段是推送内容（可以是对象）  |JSON通用返回格式（status=200代表正常）|
