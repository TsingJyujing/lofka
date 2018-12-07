using log4net;
using log4net.Config;
using System;
using System.IO;
using System.Reflection;
using System.Threading;

namespace Example.Log4net.HttpAsyncAppender
{
    class Program
    {
        static void Main(string[] args)
        {
            Thread.CurrentThread.Name = "main";
            var configFile = new FileInfo("log4net.config");
            var assembly = Assembly.GetAssembly(typeof(Lofka.Dotnet.Log4net.HttpAsyncAppender));//加载Lofka.Dotnet.Log4net应用程序集
            var repository = LogManager.GetRepository(assembly);
            XmlConfigurator.Configure(repository, configFile);//初始化配置
            var log = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);
            for (int i = 0; i < 1000; i++)
            {
                log.Info("I'm INFO log...");
                log.Error("I'm ERROR log...");
                Console.WriteLine(i);
            }
            Console.ReadLine();
        }
    }
}
