# 配置一台Lofka服务器

如果要统一收集日志，首先要有一台日志服务器。

## Lofka服务器构成
一台Lofka服务器由下列服务组成

- Http日志服务器（默认端口9500）
    - 单个文档推送接口（压缩/不压缩）
    - 多个文档推送接口（压缩/不压缩）
    - Websocket日志推流
    - 首页、监控、其他接口
- Socket服务器推送接口
    - Log4j 1.x SocketServer
    - LogBack SocketServer
    - ~~Log4j 2.x SocketServer~~

## 启动服务器

### 编译服务器

需要Java8。

[直接下载Lofka服务端](http://file.lovezhangbei.top/lofka/lofka-server-1.6.jar)

首先使用如下指令编译服务器：
```bash
mvn clean package -pl lofka-server -am
```

随后在lofka-server/target 中的lofka-server-xxx.jar就是服务器程序，直接执行即可运行日志服务器。

### 配置服务器

将项目中conf目录下的文件拷贝到和jar包统一目录进行编辑：


#### 参数配置
一般的参数都通过`lofka.properties`进行配置：

- 应用名称配置 ：`lofka.application`，如果不需要就空着或者不写这一项配置

如果配置有名称，那么系统在收到日志的时候自动会缀上名称。
例如：服务名称叫`test`,app_name字段叫`nginx`，那么会自动变为`test/nginx`。

#### Queue配置
通过修改配置文件，即可控制Lofka服务器在收到日志之后的行为。

##### Kafka持久化配置
该项作用为将所有收到的日志写入某一个Kafka中。
通过配置`lofka-kafka.properties`文件进行配置，没有这个配置文件或者配置文件错误就不会启动Kafka队列写入。
配置内容一般为：
```properties
bootstrap.servers=data1.cvnavi-test.com:9092,data2.cvnavi-test.com:9092,data3.cvnavi-test.com:9092
key.serializer=org.apache.kafka.common.serialization.IntegerSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
compression.type=gzip
logger.topic=logger-json
```
其中，`bootstrap.servers`和`logger.topic`一般随着目标Kafka的不同而不同。

##### Redirect转发配置
该项作用为将收到的所有日志（异步地）推送到另一个Lofka服务器
通过配置`lofka-redirect.properties`文件进行配置，没有这个配置文件或者配置文件错误就不会启动转发。
```properties
redirect.target=http://lofka.example.com/
```
通过配置`redirect.target`来配置转发目标。

#### Kafka日志接收配置
HttpServer还提供了WebSocket服务器来推送日志，需要编辑`lofka-kafka-client.properties`：

```properties
bootstrap.servers = data1.cvnavi-test.com:9092,data2.cvnavi-test.com:9092,data3.cvnavi-test.com:9092
group.id=logger-json-server-consumer
enable.auto.commit=false
auto.commit.interval.ms=99999999
key.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
/*value.deserializer=org.apache.kafka.common.serialization.StringDeserializer*/
# My config
kafka.topic=logger-json
```


### 运行服务器
首先在jar包同一目录下需要有一个conf文件夹，配置文件都需要放在这个文件夹内，启动应用的时候需要设置WorkingDirectory为conf（或者jar包）所在的文件夹。

这是笔者的日志服务器配置，由于不需要转发，lofka-redirect.properties后面加上了template屏蔽之。
```
.
├── conf
│   ├── lofka-kafka-client.properties
│   ├── lofka-kafka.properties
│   ├── lofka.properties
│   └── lofka-redirect.properties.template
└── logger-server.jar
```

运行服务器：
```bash
java -jar lofka-server-*.jar
```
如果需要以其他端口运行，命令行中增加配置： `--server.port=你要的端口`


### Nginx配置

推荐的Nginx配置如下，可根据自己的需要进行酌情修改（比如支持负载均衡什么的啦～）

```nginx
upstream lofka{
    server 127.0.0.1:9500;
}

server {
    listen       80;
    server_name  logger.example.com;
    client_max_body_size 100m;

    # 配置日志接受接口
    location / {
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_pass    http://lofka;
        client_max_body_size    1000m;
    }

    # 配置WebSocket日志推送接口
    location ~ /lofka/websocket/logs {
        proxy_pass            http://lofka;
        proxy_set_header      Host $host;
        proxy_http_version    1.1;
        proxy_set_header      Upgrade "websocket";
        proxy_set_header      Connection "Upgrade";
        proxy_read_timeout    86400;
    }
}
```

## 持久化到MongoDB
参考文件夹`lofka-mongo-writer`中的内容，修改`lofka-mongo-writer/kafka-config.json`为你的环境的配置。
随后直接启动脚本：`python3 mongodb_writer.py`
即可完成入库到MongoDB的服务
