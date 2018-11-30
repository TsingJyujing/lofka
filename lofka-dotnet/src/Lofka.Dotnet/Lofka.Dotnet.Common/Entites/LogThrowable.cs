using System.Collections.Generic;
using Newtonsoft.Json;

namespace Lofka.Dotnet.Common.Entites
{
    /// <summary>
    /// lofka异常信息
    /// 消息格式由lofka定义,详细请参考<see cref="https://github.com/TsingJyujing/lofka/wiki/LoggerJSON格式规范"/>
    /// </summary>
    public class LogThrowable
    {
        /// <summary>
        /// 异常产生的消息
        /// </summary>
        [JsonProperty(PropertyName = "message")]
        public string Message { set; get; }

        /// <summary>
        /// 堆栈追踪列表
        /// </summary>
        [JsonProperty(PropertyName = "stack_trace")]
        public List<LogLocation> StackTrace { set; get; } = new List<LogLocation>();

    }
}
