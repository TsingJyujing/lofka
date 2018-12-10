using log4net;
using log4net.Config;
using System;
using System.IO;
using System.Reflection;
using System.Threading;

namespace Example.Log4net.Net4.HttpAsyncAppender
{
    class Program
    {
        static void Main(string[] args)
        {
            Thread.CurrentThread.Name = "main";
            var configFile = new FileInfo("log4net.config");
            var assembly = Assembly.GetAssembly(typeof(Lofka.Dotnet.Log4net.HttpAsyncAppender));//加载Lofka.Dotnet.Log4net应用程序集
            var repository = LogManager.GetRepository(assembly);
            var col = XmlConfigurator.Configure(repository, configFile);//初始化配置
            var log = LogManager.GetLogger("SQ");
            for (int i = 0; i < 100000; i++)
            {
                log.Info($"I'm Info log {i}...");
                log.Debug($"I'm Debug log {i}...");
                log.Error($"I'm Error log {i}...");
                log.Warn($"I'm Warn log {i}...");
                log.Fatal($"I'm Fatal log {i}...");
                Console.WriteLine(i);
            }
            Console.ReadLine();
        }
    }
}
