# 文本日志回收器-LofkaTail

```
 _              __  _            _____       _  _
| |            / _|| |          |_   _|     (_)| |
| |      ___  | |_ | | __ __ _    | |  __ _  _ | |
| |     / _ \ |  _|| |/ // _` |   | | / _` || || |
| |____| (_) || |  |   <| (_| |   | || (_| || || |
\_____/ \___/ |_|  |_|\_\\__,_|   \_/ \__,_||_||_|
```
## 启动命令与参数设置
启动的时候默认会显示一些帮助：
```
options    type     usage                          default
--file     string   which file to monitor          [no default]
--target   string   url of logger server           http://logger.example.com/
--period   float    the period to scan the file    1.0
--type     list[]   which processors will be used  common
--app_name string   application name               lofka_tail
--append   string   a json file name to read       [no default]
```

这里给出更加详细的解释：

（选项都要加上`--`）

|选项|类型|详细说明|默认值|备注|
|-|-|-|-|-|
|file   |字符串   |选择要监控哪个文件   |没有默认值   |-|
|target   |字符串   |写入到那个日志服务器（http://logger.example.com/）   | 没有默认值   |-|
|period   |浮点型   |文件扫描周期（单位：秒）   | 1.0  |实时性要求高可以换成0.3之类|
|type   |字符串列表（逗号分隔）   |使用哪些解析器对日志进行解析   | common |可选的还有nginx和arg_common，也可以自己扩展|
|app_name   |字符串   |应用名称   | lofka_tail   |-|
|append   |字符串   |附加JSON文件的名称，JSON中的数据将会被完整的添加到每个日志文件中   | 没有默认值   |如果字段和其他程序产生字段（如timestamp或者app_name）有冲突将会被覆盖|

**需要注意的是，http://logger.example.com/ 也是使用Nginx进行代理的，在对代理 http://logger.example.com/ 的Nginx进行日志收集的时候，务必将日志直接收集到 http://127.0.0.1:9500/ 否则将引起自激振荡。**

## 通用回收（`--type common`）
通用回收器用于回收一般的日志，一般都是不推荐这么回收的，除非该日志的配置既没有办法改动，也很难通过文本操作解析。

## 通用参数化回收（`--type arg_common`）
有一种很简单的方式针对已有的日志进行格式化改造，只需要输出为`--key  value`格式即可，其中，key和value之间空两个Tab键，如果你的日志可能会输出连续的2个Tab键……这么变态的日志谁写的啊～请自己修改源代码解析日志吧。
例如某日志系统就可以这么设置：
`--time\t\t%t\t\t--message\t\t%s`

## Nginx（`--type nginx`）
Nginx解析器根据Nginx的一些特性做了针对性的优化，对字段进行了更加深度解析。
要想记录Nginx的日志需要做以下事情：

1. 修改Nginx的配置文件（通常是/etc/nginx/nginx.conf）
2. 验证配置文件准确性（nginx -t）
3. 重新加载Nginx（nginx -s reload）
4. 启动监听工具

### Nginx配置修改
推荐的Nginx日志如下所示，更多详细信息请参见官方文档：

1. [Nginx核心模块参数](http://nginx.org/en/docs/http/ngx_http_core_module.html#variables)
2. [Nginx Upstream 模块参数](http://nginx.org/en/docs/http/ngx_http_upstream_module.html#variables)
3. 等等

```nginx
##
# Logging Settings

log_format	main	'--remote_addr\t\t$remote_addr\t\t'
                    '--remote_user\t\t$remote_user\t\t'
                    '--time_iso8601\t\t$time_iso8601\t\t'
                    '--request_method\t\t$request_method\t\t'
                    '--request_length\t\t$request_length\t\t'
                    '--scheme\t\t$scheme\t\t'
                    '--server_protocol\t\t$server_protocol\t\t'
                    '--uri\t\t$uri\t\t'
                    '--args\t\t$args\t\t'
                    '--status\t\t$status\t\t'
                    '--bytes_sent\t\t$bytes_sent\t\t'
                    '--body_bytes_sent\t\t$body_bytes_sent\t\t'
                    '--http_referer\t\t$http_referer\t\t'
                    '--http_user_agent\t\t$http_user_agent\t\t'
                    '--http_x_forwarded_for\t\t$http_x_forwarded_for\t\t'
                    '--upstream_addr\t\t$upstream_addr\t\t'
                    '--upstream_response_time\t\t$upstream_response_time\t\t'
                    '--request_time\t\t$request_time\t\t'
                    '--msec\t\t$msec\t\t'
                    '--pipe\t\t$pipe\t\t'
                    '--host\t\t$host\t\t'
                    ;
```

### 启动监听工具
启动监听日志：
```bash
sudo python lofka_tail.py --file /var/log/nginx/access.log --type nginx --app_name 服务器的标识名称 --target http://logger.example.com/
```
