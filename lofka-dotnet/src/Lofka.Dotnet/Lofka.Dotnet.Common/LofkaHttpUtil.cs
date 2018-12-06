using System;
using System.IO;
using System.IO.Compression;
#if NET45 || NETSTANDARD20
using System.Net.Http;
#elif NET40
using System.Net;
#endif
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
#if NET45 || NETSTANDARD20
            Client = new HttpClient();
            Client.DefaultRequestHeaders.Connection.Add("keep-alive");
#endif
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
        public Uri BaseUri
        {

#if NET45 || NETSTANDARD20
            set
            {
                Client.BaseAddress = value;
            }
#elif NET40
            set; private get;
#endif
        }
#if NET45 || NETSTANDARD20
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
#elif NET40
        private HttpWebRequest request;
        /// <summary>
        /// 向服务器发送日志数据
        /// </summary>
        /// <param name="target">目标</param>
        /// <param name="strPostData">日志数据json字符串</param>
        /// <param name="isCompress">是否压缩</param>
        /// <returns>true:发送成功;false:失败</returns>
        public bool PostData(string target, string strPostData, bool isCompress)
        {
            try 
	        {	        
		        var url=new Uri(BaseUri,target).ToString();
                if (BaseUri.ToString().StartsWith("https",StringComparison.OrdinalIgnoreCase))
	            {
                    request=WebRequest.Create(url) as HttpWebRequest;
	            }
                else
	            {
                    request=HttpWebRequest.Create(url) as HttpWebRequest;
	            }
                request.Method="POST";
                request.Headers.Add("Accept-Charset","utf-8");
                var postData = Encoding.UTF8.GetBytes(strPostData);
                if (isCompress)
	            {
                    request.Headers.Add("Accept-Encoding","gzip");
                    postData = CompressData(postData);
	            }
                request.ContentLength=postData.Length;
                using(var stream=request.GetRequestStream())
	            {
                    stream.Write(postData,0,postData.Length);
	            }
                var response = request.GetResponse() as HttpWebResponse;
                return response.StatusCode==HttpStatusCode.OK;
	        }
	        catch //(Exception ex)
	        {
                return false;
	        }
        }
#endif
        /// <summary>
        /// 压缩数据
        /// </summary>
        /// <param name="original">压缩前的原始数据</param>
        /// <returns></returns>
        private static byte[] CompressData(byte[] original)
        {
            using (var stream = new MemoryStream())
            {
                using (var gzipStream = new GZipStream(stream, CompressionMode.Compress, false))
                {
                    gzipStream.Write(original, 0, original.Length);
                }
                var compressed = stream.ToArray();
                return compressed;
            }
        }
    }
}
