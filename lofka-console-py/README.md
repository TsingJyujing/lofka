# 日志显示工具

本质上是对截取的JSON数据进行合理的过滤和格式化，可以通过编写Python脚本的方式完成你的格式化.

## 环境准备
首先你需要安装一下Python3,python-snappy和kafka-python：

```bash
pip3 install kafka-python python-snappy
```

需要注意的是，对于Linux系统，安装python-snappy之前需要安装`snappy`开发库：
```bash
# 针对Debian系列系统
apt install libsnappy-dev
# 你要是加入了CentOS邪教的话
yum install snappy-devel
# 你要是用苹果
# 那么我也不知道
# 欢迎补充
```


如果是Windows，需要安装Visual C++ 14编译器，否则pip无法编译snappy。

参考资料：
- [请问如何在windows上使用python-snappy](https://www.v2ex.com/amp/t/85685)
- [python-snappy 0.5.2](https://pypi.org/project/python-snappy/)


## 实时监控日志

随后运行lofka_console.py，其中参数的格式为：`--key1.key2...keyM value1,value2,...,valueN`
举个例子，我只要监控app_name为app1或app2的WARN级别以上的日志，并且只监控固定IP送来的信息，只需要这样给出参数即可：
```bash
python3 lofka_console.py --app_name app1,app2 --level WARN,ERROR,FATAL --host.ip 10.10.11.75,10.10.11.36
```

详细的过滤取决于你对JSON的格式的定义，一般的定义**参见Wiki中的Logger-JSON格式规范**

自认为显示效果已经算是不错了，如果你有更加变态的需求请修改这个脚本，如果不会修改请联系我。

![日志效果](日志效果.png)

## 历史日志查询

用法和实时显示日志工具差不多，区别在于多了几个参数：
- `limit`：限制显示的条数，如果不设置或者设置的数字小于等于0则不限制
- `head`：布尔型（只需要输入`--head`则为True，否则为False），用来定义显示开头的N条还是最后的N条，需要和limit联用。
- `start`和`end`：开始时间和结束时间，格式为`yyyyMMddhhmmss`，允许不写全，例如`20180101`等价于`20180101000000`，但是不能只写一半，例如`2018121`就是非法的。

其余约束和实时日志一样，区别在于，只能对"app_name","logger","host.ip","thread","level","host.name"这几个字段做过滤（同时不推荐对"host.name"做过滤，会比较慢）
