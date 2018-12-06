using log4net.Appender;
using log4net.Core;
using System;
using System.Collections.Generic;
namespace Lofka.Dotnet.Log4net
{
    using Common;
    using Common.Entites;
    using Newtonsoft.Json;
    using System.Threading;

    /// <summary>
    /// log4net Http多条/异步日志适配器
    /// 日志将先进入日志缓存，当达到一定数量或距离上次推送达到一定时间间隔，才会执行日志推送
    /// 截至2018-08-27 lofka多条日志接口定义
    /// 无压缩日志提交接口:/lofka/service/push/batch
    /// 压缩日志提交接口:/lofka/service/push/batch/zip
    /// </summary>
    public class HttpAsyncAppender : AppenderSkeleton
    {
        /// <summary>
        /// 构造函数
        /// </summary>
        public HttpAsyncAppender()
        {
            workThread = new Thread(Check)
            {
                IsBackground = true,
                Name = "workThread"
            };
            workThread.Start();
            lastPushTime = DateTime.Now;
        }
        /// <summary>
        /// http处理对象
        /// </summary>
        private LofkaHttpUtil httpUtil;
        /// <summary>
        /// 工作线程
        /// </summary>
        private Thread workThread;
        private DateTime lastPushTime;
        /// <summary>
        /// 工作状态
        /// </summary>
        public bool Working { get; set; } = true;
        /// <summary>
        /// 应用程序/模块名称
        /// </summary>
        public string Application { set; get; }
        /// <summary>
        /// 服务器主机地址 例如:
        /// <example>http://lofka.example.com</example> 
        /// </summary>
        public string ServerHost { set; get; }
        /// <summary>
        /// 无压缩日志推送接口地址，例如 <example>/lofka/service/push/batch</example>.
        /// 由lofka定义接口地址，具体请参考
        /// <see cref="https://github.com/TsingJyujing/lofka/wiki/Http接口文档"/>
        /// </summary>
        public string Target { set; get; }
        /// <summary>
        /// 压缩日志推送接口地址，例如 <example>/lofka/service/push/batch/zip</example>.
        /// 由lofka定义接口地址，具体请参考
        /// <see cref="https://github.com/TsingJyujing/lofka/wiki/Http接口文档"/>
        /// </summary>
        public string CompressTarget { set; get; }
        /// <summary>
        /// 是否自动压缩(启用自动压缩，日志缓存里超过)
        /// </summary>
        public bool AutoCompress { set; get; } = true;
        /// <summary>
        /// 缓存最大大小 
        /// 当缓存达到最大大小的时候，程序会自动将缓存里的日志一次全部推送给日志服务器
        /// MaxSize 与Interval 只要有一个条件成立，就会执行日志自动推送
        /// </summary>
        public int MaxSize { set; get; } = 1024;
        /// <summary>
        /// 日志推送间隔 单位 毫秒
        /// 当最近一次推送到当前时间达到推送间隔时，程序会自动将缓存里的日志一次全部推送给日志服务器
        /// MaxSize 与Interval 只要有一个条件成立，就会执行日志自动推送
        /// </summary>
        public int Interval { set; get; } = 500;
        /// <summary>
        /// 日志缓存
        /// </summary>
        private readonly List<LoggerInfo> logBuffer = new List<LoggerInfo>();
        /// <summary>
        /// 
        /// </summary>
        /// <param name="loggingEvent"></param>
        protected override void Append(LoggingEvent loggingEvent)
        {
            if (loggingEvent != null)
            {
                lock (logBuffer)
                {
                    logBuffer.Add(loggingEvent.ToLoggerInfo(Application));
                    if (logBuffer.Count > MaxSize)
                    {
                        PushToServer();
                    }
                }

            }
        }
        /// <summary>
        /// 将日志日志推送至服务器
        /// </summary>
        private void PushToServer()
        {
            string jsonData = string.Empty;
            lastPushTime = DateTime.Now;
            lock (logBuffer)
            {
                if (logBuffer.Count <= 0)
                {
                    return;
                }
                jsonData = JsonConvert.SerializeObject(logBuffer);
                logBuffer.Clear();
            }
            if (!string.IsNullOrEmpty(jsonData))
            {
                if (httpUtil is null)
                {
                    CreateHttpUtil();
                }
                var isCompress = false;
                var target = Target;
                if (jsonData.Length > 1024)//超过1024字节（1K）
                {
                    isCompress = true;
                    target = CompressTarget;
                }
                var times = 0;
                var isComplete = false;
                do
                {
                    if (times > 0)
                    {
                        Thread.Sleep(1000);
                    }
                    times++;
#if NET40
                    isComplete=httpUtil.PostData(Target, jsonData, isCompress);
#else
                    isComplete = httpUtil.PostDataAsync(Target, jsonData, isCompress).Result;
#endif

                } while (!isComplete && times <= 3);//如果失败尝试3次
            }
        }

        /// <summary>
        /// 创建http辅助对象
        /// </summary>
        private void CreateHttpUtil()
        {
            httpUtil = new LofkaHttpUtil(ServerHost);
        }
        /// <summary>
        /// 时间间隔检查
        /// </summary>
        private void Check()
        {
            while (Working)
            {
                if ((DateTime.Now - lastPushTime).TotalMilliseconds > Interval)
                {
                    PushToServer();
                }
                Thread.Sleep(10);
            }
        }
    }
}
