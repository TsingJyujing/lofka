using System;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

namespace Lofka.Dotnet.Common.Entites
{
    /// <summary>
    /// 日志产生所在的位置 
    /// 消息格式由lofka定义,详细请参考<see cref="https://github.com/TsingJyujing/lofka/wiki/LoggerJSON格式规范"/>
    /// </summary>
    public class LogLocation
    {
        /// <summary>
        /// 方法名称
        /// </summary>
        [JsonProperty(PropertyName = "method")]
        public string MethodName { set; get; }
        /// <summary>
        /// 行号
        /// </summary>
        [JsonProperty(PropertyName = "line")]
        public int Line { set; get; }
        /// <summary>
        /// 文件名称
        /// </summary>
        [JsonProperty(PropertyName = "filename")]
        public string FileName { set; get; }
        /// <summary>
        /// 类名称
        /// </summary>
        [JsonProperty(PropertyName = "class")]
        public string ClassName { set; get; }
    }
}
