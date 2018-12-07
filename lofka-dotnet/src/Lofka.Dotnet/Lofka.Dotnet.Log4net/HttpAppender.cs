using log4net.Appender;
using log4net.Core;

namespace Lofka.Dotnet.Log4net
{
    using Common;
    using System.Threading;

    /// <summary>
    /// log4net Http单条日志适配器
    /// 截至2018-08-27 lofka单条日志接口定义
    /// 无压缩日志提交接口:/lofka/service/push
    /// 压缩日志提交接口:/lofka/service/push/zip
    /// </summary>
    public class HttpAppender : AppenderSkeleton
    {

        private LofkaHttpUtil httpUtil;
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
        /// 日志提交接口地址，例如 <example>/lofka/service/push</example>.
        /// 由lofka定义接口地址，具体请参考
        /// <see cref="https://github.com/TsingJyujing/lofka/wiki/Http接口文档"/>
        /// </summary>
        public string Target { set; get; }
        /// <summary>
        /// 是否压缩(默认false)
        /// </summary>
        public bool IsCompress { set; get; } = false;
        /// <summary>
        /// 处理日志
        /// </summary>
        /// <param name="loggingEvent"></param>
        protected override void Append(LoggingEvent loggingEvent)
        {
            if (httpUtil is null)
            {
                CreateHttpUtil();
            }
            var loginfo = loggingEvent.ToLoggerInfo(Application);
            if (loginfo != null)
            {
                var jsonData = loginfo.ToJson();
                var times = 0;
                bool isComplete = false;
                do
                {
                    if (times > 0)
                    {
                        Thread.Sleep(1000);
                    }
                    times++;
#if NET40
                    isComplete=httpUtil.PostData(Target, jsonData, IsCompress);
#else
                    isComplete = httpUtil.PostDataAsync(Target, jsonData, IsCompress).Result;
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
    }
}
