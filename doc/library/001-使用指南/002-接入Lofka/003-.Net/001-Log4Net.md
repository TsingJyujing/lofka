# Log4net

本项目是基于 log4net 2.0.8版本开发，兼容.net Framework 4.5+ 和.net core
相信服务器端应用程序一般都应该升级到.net Framework 4.5以上版本了。
其他版本只有等以后有时间再来实现了.

项目包含两个适配器 [HttpAppender](#httpappender) 和[HttpAysncAppender](#httpaysncappender)
## HttpAppender
HttpAppender适配器通常用于日志产生不频繁，且日志量比较小的情况。基本上产生日志就会把日志内容推送至lofka日志服务器,基本不存在延迟的情况（应用与服务器之间网络状况比较差，暂不在考虑范围emmmmm~）。
### 配置
和log4net官方配置很相像，只是需要针对HttpAppender适配器做出额外配置。主要集中在 **`appeder`** 节点上，下面是一个简单的HttpAppender配置：
```xml
<?xml version="1.0" encoding="utf-8" ?>
<log4net>
  <appender name="HttpAppender" type="Lofka.Dotnet.Log4net.HttpAppender,Lofka.Dotnet.Log4net">
    <!--应用程序名称-->
    <param name="Application" value="Example/Log4net/HttpAppender"/>
    <!--日志服务地址-->
    <param name="ServerHost" value="http://lofka.example.com"/>
    <!--日志推送地址-->
    <param name="Target" value="/lofka/service/push"/>
    <!--是否启用压缩 可选项，默认false-->
    <param name="IsCompress" value="false"/>
  </appender>
  <root>
    <level value="ALL" />
    <appender-ref ref="HttpAppender" />
  </root>
</log4net>
```
无需layout配置，lofka统一管理日志存储格式。（至于个性化的显示格式，你可以根据需要参考lofka日志格式自行修改。）
### 使用方法
以下是一个简单的示例代码，主要作用是输出一句Info级别的日志，以及异常日志记录。
```csharp
class Program
{
    static void Main(string[] args)
    {
        var config = new FileInfo("log4net.config");
        var assembly = Assembly.GetAssembly(typeof(Lofka.Dotnet.Log4net.HttpAppender));//加载Lofka.Dotnet.Log4net应用程序集
        var repository = LogManager.GetRepository(assembly);
        XmlConfigurator.Configure(repository, config);//初始化配置
        var log = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
        log.Info("Hello,this is Example");
        try
        {
            var val = int.Parse("abc");
        }
        catch (FormatException ex)
        {
            log.Error("Error!", ex);
        }
        Console.ReadLine();
    }
}
```

## HttpAysncAppender
HttpAysncAppender 是一个异步的适配器，特点是，收到日志以后不会立即向lofka服务器推送日志。而是先将日志信息存放在日志缓存中，当积攒到指定数量或到达指定的时间间隔，才会将缓存中的日志推送至lofka服务器，并且默认会根据推送内容大小（目前写死1024字节）自动进行压缩。

特别适用于日志内容比较大，并且频繁产生的情况。
### 配置
以下是一个简单的HttpAsyncAppender配置
```xml
<?xml version="1.0" encoding="utf-8" ?>
<log4net>
  <appender name="HttpAsyncAppender" type="Lofka.Dotnet.Log4net.HttpAsyncAppender,Lofka.Dotnet.Log4net">
    <!--应用程序名称-->
    <param name="Application" value="Example/Log4net/HttpAsyncAppender"/>
    <!--日志服务地址-->
    <param name="ServerHost" value="http://lofka.example.com"/>
    <!--不压缩日志推送地址-->
    <param name="Target" value="/lofka/service/push/batch"/>
    <!--压缩日志推送地址-->
    <param name="CompressTarget" value="/lofka/service/push/batch/zip"/>
    <!--自动压缩 单次提交超过1KB在的时候会自动使用Gzip压缩 默认开启 如果为false则表示不压缩-->
    <param name="AutoCompress" value="true"/>
    <!--日志缓存最大条数-->
    <param name="MaxSize" value="30"/>
    <!--时间间隔 单位毫秒-->
    <param name="Interval" value="10000"/>
  </appender>
  <root>
    <level value="ALL" />
    <appender-ref ref="HttpAsyncAppender" />
  </root>
</log4net>
```
不建议对 ***Target*** 和 ***CompressTarget*** 的配置进行修改，除非你修改了lofka的核心代码。
### 使用方法
和[HttpAppender](#httpappender)的示例程序一样，只是为了达到批量推送日志的目的，在原有的输出Info级别和异常日志的基础上增加了一个90次的循环。

```csharp
 class Program
 {
     static void Main(string[] args)
     {
         Thread.CurrentThread.Name = "main";
         var config = new FileInfo("log4net.config");
         var assembly = Assembly.GetAssembly(typeof(Lofka.Dotnet.Log4net.HttpAsyncAppender));//加载Lofka.Dotnet.Log4net应用程序集
         var repository = LogManager.GetRepository(assembly);
         XmlConfigurator.Configure(repository, config);//初始化配置
         var log = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
         for (int i = 0; i < 90; i++)
         {
             log.Info("Hello,this is Example");
             try
             {
                 var val = int.Parse("abc");
             }
             catch (FormatException ex)
             {
                 log.Error(ex.ToString(), ex);
             }
         }
         Console.ReadLine();
     }
 }
```
## 特别说明
 我发现使用 `HttpAppender` 和 `HttpAysncAppender` 过程中，需要加载自定义配置文件，而加载配置文件以需要把`Lofka.Dotnet.Log4net`加载进系统,不然有时候会报log4net无法加载应用程序集`Lofka.Dotnet.Log4net`,并且通过调试发现无法进入自定义appender的Append方法。这个问题曾困扰我很久=====(￣▽￣*)b 可以参考如下代码来加载Lofka.Dotnet.Log4net应用程序集
####  HttpAppender

```Csharp
var config = new FileInfo("log4net.config");
var assembly = Assembly.GetAssembly(typeof(Lofka.Dotnet.Log4net.HttpAppender));//加载Lofka.Dotnet.Log4net应用程序集
var repository = LogManager.GetRepository(assembly);
XmlConfigurator.Configure(repository, config);//初始化配置
```
####  HttpAysncAppender
```Csharp
var config = new FileInfo("log4net.config");
var assembly = Assembly.GetAssembly(typeof(Lofka.Dotnet.Log4net.HttpAsyncAppender));//加载Lofka.Dotnet.Log4net应用程序集
var repository = LogManager.GetRepository(assembly);
XmlConfigurator.Configure(repository, config);//初始化配置
```

## 写在最后
我觉得这两个Appender 各有优缺点，HttpAysncAppender 如果放在应用程序退出的时候输出日志，那么很有可能会丢失日志，HttpAppender的网络开销又太大，不适合大规模使用，所以理论上 HttpAppender 和 HttpAysncAppender  应该配合使用，本项目理论上也支持配合使用，但我个人没试过。━┳━　━┳━ 后面再写一个配合使用的示例.
