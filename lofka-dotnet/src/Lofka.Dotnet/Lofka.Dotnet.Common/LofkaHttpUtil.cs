using System;
using System.IO;
using System.IO.Compression;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace Lofka.Dotnet.Common
{
    /// <summary>
    /// http辅助工具
    /// </summary>
    public class LofkaHttpUtil
    {
        /// <summary>
        /// 构造函数
        /// </summary>
        public LofkaHttpUtil()
        {
            Client = new HttpClient();
            Client.DefaultRequestHeaders.Connection.Add("keep-alive");
        }
        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="baseUrl">HttpClient的BaseAddress</param>
        public LofkaHttpUtil(string baseUrl) : this()
        {
            BaseUri = new Uri(baseUrl);
        }
        /// <summary>
        /// http客户端的BaseAddress
        /// </summary>
        public Uri BaseUri { set { Client.BaseAddress = value; } }
        /// <summary>
        /// http客户端
        /// </summary>
        private HttpClient Client { set; get; }
        /// <summary>
        /// 向服务器发送日志数据
        /// </summary>
        /// <param name="target">目标</param>
        /// <param name="strPostData">日志数据json字符串</param>
        /// <param name="isCompress">是否压缩</param>
        /// <returns>true:发送成功;false:失败</returns>
        public async Task<bool> PostDataAsync(string target, string strPostData, bool isCompress)
        {
            if (!string.IsNullOrEmpty(strPostData.Trim()))
            {
                var postData = Encoding.UTF8.GetBytes(strPostData);

                if (isCompress)
                {
                    postData = CompressData(postData);
                }
                var content = new ByteArrayContent(postData);
                var response = await Client.PostAsync(target, content);

                Console.WriteLine(strPostData);
                return response.IsSuccessStatusCode;
            }
            else
            {
                //提交日志数据为空，无需提交数据所以直接返回成功
                return true;
            }
        }
        /// <summary>
        /// 压缩数据
        /// </summary>
        /// <param name="original">压缩前的原始数据</param>
        /// <returns></returns>
        private static byte[] CompressData(byte[] original)
        {
            using (var stream = new MemoryStream())
            {
                using (var gzipStream = new GZipStream(stream, CompressionMode.Compress,false))
                {
                    gzipStream.Write(original, 0, original.Length);
                }
                var compressed = stream.ToArray();
                return compressed;
            }
        }
    }
}
