# MongoDB持久化工具
直接运行即可，注册为服务的话配置如下：

```
[Unit]
Description=Logger MongoDB Writer
After=network.target,kafka.service

[Service]
User=root
Group=root
ExecStart=/usr/bin/python3 /opt/logger/backend-server/mongodb_writer.py >/var/log/logger-mongodb-writer.log 2>&1
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
