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
            XmlConfigurator.Configure(repository, configFile);//初始化配置
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
}
