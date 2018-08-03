# 日志服务器前端

## 简介

本程序用来接受各种各样的日志输入，接受输入的方式有以下几种：

- Log4j 1.x Socket Server
- Log4j 2.x Socket Server
- Logback Socket Server
- HTTP Server

其中，HTTP Server还支持向其他Topic直接写入流。

详细使用方法请参见Wiki。

目前的部署信息：
- Log4j 1.x Socket Server 端口为 9501
- Log4j 2.x Socket Server 端口将为 9502 (但是需要特殊的Appender，参见Wiki)
- Logback Socket Server 端口为 9503
- HTTP Server 端口为 9500

## 监控接口
`lofka/socket/servers/monitor`

使用GET方法请求，获取JSON格式的数据。

## 服务注册

配置文件如下：

```
[Unit]
Description=LoggerServer
After=network.target,mongod.service,kafka.service

[Service]
User=root
Group=root
ExecStart=/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar /opt/logger/front-server/logger-server.jar >/var/log/logger-server.log 2>&1
PIDFile=/var/run/logger-server.pid
# file size
LimitFSIZE=infinity
# cpu time
LimitCPU=infinity
# virtual memory size
LimitAS=infinity
# open files
LimitNOFILE=64000
# processes/threads
LimitNPROC=64000
# locked memory
LimitMEMLOCK=infinity
# total threads (user+kernel)
TasksMax=infinity
TasksAccounting=false

[Install]
WantedBy=multi-user.target
```
