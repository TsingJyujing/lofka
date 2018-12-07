using Newtonsoft.Json;

namespace Lofka.Dotnet.Common.Entites
{
    /// <summary>
    /// 主机信息，
    /// 由lofka定义的日志格式，详细请参考<see cref="https://github.com/TsingJyujing/lofka/wiki/LoggerJSON格式规范"/>
    /// </summary>
    public class HostInfo
    {
        /// <summary>
        /// 主机名称
        /// </summary>
        [JsonProperty(PropertyName = "name")]
        public string Name { set; get; }
        /// <summary>
        /// 主机IP
        /// </summary>
        [JsonProperty(PropertyName = "ip")]
        public string IP { set; get; }
    }
}
