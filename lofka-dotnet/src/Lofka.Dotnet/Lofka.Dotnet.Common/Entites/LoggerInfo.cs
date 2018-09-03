using Newtonsoft.Json;

namespace Lofka.Dotnet.Common.Entites
{
    /// <summary>
    /// 日志信息，
    /// 由lofka定义的日志格式，详细请参考<see cref="https://github.com/TsingJyujing/lofka/wiki/LoggerJSON格式规范"/>
    /// </summary>

    public class LoggerInfo
    {
        /// <summary>
        /// 日志等级，必需是TRACE, DEBUG, INFO, WARN, ERROR, FATAL这几个等级
        /// </summary>
        [JsonProperty(PropertyName = "level")]
        [JsonRequired]
        public string Level { set; get; }
        /// <summary>
        /// 日志信息
        /// </summary>
        [JsonProperty(PropertyName = "message")]
        [JsonRequired]
        public string Message { set; get; }
        /// <summary>
        /// 日志产生的时间，单位是毫秒，如果有纳秒的信息请放在小数位
        /// </summary>
        [JsonProperty(PropertyName = "timestamp")]
        public double TimeStamp { set; get; }
        ///// <summary>
        ///// 过期时间，逾期删除（）
        ///// </summary>
        //[JsonProperty(PropertyName = "expired_time")]
        //public double ExpiredTime { set; get; }
        ///// <summary>
        ///// 主机信息
        ///// </summary>
        //[JsonProperty(PropertyName = "host")]
        //public HostInfo Host { set; get; }
        /// <summary>
        /// 日志名称
        /// </summary>
        [JsonProperty(PropertyName = "logger")]
        public string LoggerName { set; get; }
        /// <summary>
        /// 线程名称
        /// </summary>
        [JsonProperty(PropertyName = "thread")]
        public string ThreadName { set; get; }
        /// <summary>
        /// 应用程序/模块名称
        /// </summary>
        [JsonProperty(PropertyName = "app_name")]
        public string AppName { set; get; }
        /// <summary>
        /// 日志产生所在的位置
        /// </summary>
        [JsonProperty(PropertyName = "location")]
        public LogLocation Location { set; get; }
        /// <summary>
        /// 异常信息
        /// </summary>
        [JsonProperty(PropertyName = "throwable")]
        public LogThrowable Throwable { set; get; }
        /// <summary>
        /// 
        /// </summary>
        [JsonProperty(PropertyName = "type")]
        public string LogType { set; get; } = "LOG4NET";
        /// <summary>
        /// 转JSON字符串
        /// </summary>
        /// <returns></returns>
        public string ToJson()
        {
            return JsonConvert.SerializeObject(this);
        }
    }
}
