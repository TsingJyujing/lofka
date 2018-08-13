# Java 接入概述

除非使用原生的SocketAppender（极不推荐）之外，都需要引入LofkaUtils库：

```xml
<dependency>
    <groupId>com.github.tsingjyujing</groupId>
    <artifactId>lofka-utils</artifactId>
    <version>RELEASE</version>
</dependency>
```

如果你下载Lofka中出现异常或者有其它问题可以手动安装：

```bash
mvn clean install -pl lofka-utils -am
```

同时请保证你的项目使用JDK 1.7以上的版本。

有如下几个参数会频繁的出现，下面不一定一一解释其意义：

|参数名称|实际意义|取值举例|
|-|-|-|
|application   |应用名称，将会出现在LoggerJSON的`app_name`字段   |lofka-app   |
|target   |Lofka服务器地址   |http://127.0.0.1:9500/ |
|interval   |异步Http发送最大间隔（毫秒）   |100   |
|maxBufferSize   |异步Http发送最大积压条数   |100   |
