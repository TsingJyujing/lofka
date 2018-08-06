# LogBack日志的无痛改造

## （墙裂推荐）增加Lofka版本的HttpAppender
你可以引入我的第三方库：

```xml
<dependency>
    <groupId>com.github.tsingjyujing</groupId>
    <artifactId>lofka-utils</artifactId>
    <version>RELEASE</version>
</dependency>
```

根据需要选择同步或者异步Appender以后，最后在ROOT段中增加这个Appender：
```xml
<appender-ref ref="LofkaHttpAppender"/>
```
其中，LofkaHttpAppender可以是你自设的Appender名称。

### 异步 Http Appender
最推荐的是使用异步的 Http Appender,本项目实现的异步HttpAppender依托于Apache的Http-Client，同时有时间触发器与日志数量触发器，将日志批量收集以后压缩（注：会自动根据正文大小选择是否使用压缩）发送到服务器，且能与服务器保持长连接以达到极高性能的日志传输。

```xml
<appender name="SOCKET" class="com.github.tsingjyujing.lofka.appender.logback.HttpAsyncAppender">
    <target>Lofka服务器地址</target>
    <applicationName>你的应用名称</applicationName>
    <interval>1000</interval><!--时间触发极限（单位：毫秒）-->
    <maxBufferSize>100</maxBufferSize><!--日志堆积量出发极限（单位：条）-->
</appender>
```

### 同步 Http Appender
如果你有较高的实时性需求，即使毫秒级的误差也会给你造成麻烦，那么你可以选择使用同步日志发送器，在每收到一条日志的时候日志系统都会单独发送。

这样配置文件变为：
```xml
<appender name="LofkaHttpAppender" class="com.github.tsingjyujing.lofka.appender.logback.HttpAppender">
    <target>Lofka服务器地址</target>
    <applicationName>你的应用名称</applicationName>
</appender>
```

## (不需要引入新的库) 增加SockerAppender
在你的logback.xml中增加一个appender。

```xml
<appender name="SOCKET" class="ch.qos.logback.classic.net.SocketAppender">
    <remoteHost>LofkaSocket服务器地址</remoteHost>
    <port>9503</port><!--默认的LogBack服务器地址-->
    <reconnectionDelay>10000</reconnectionDelay>
    <includeCallerData>true</includeCallerData>
</appender>
```

相信你是配置Logback配置文件的高手，你可以自己调节这个appender用于何处。
