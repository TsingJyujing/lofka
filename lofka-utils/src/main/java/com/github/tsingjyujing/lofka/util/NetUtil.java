package com.github.tsingjyujing.lofka.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.bson.Document;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * 网络工具
 * 用于HTTP的接收和发送，返回校验，连接管理等操作
 * <p>
 * TODO: 压缩和解压缩
 *
 * @author yuanyifan
 */
public class NetUtil {

    private static PoolingHttpClientConnectionManager CONNECTION_MANAGER = null;

    /**
     * Request相关配置
     */
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            // 连接超时 10秒
            .setConnectTimeout(10 * 1000)
            // Socket超时 1分钟
            .setSocketTimeout(60 * 1000)
            // 连接请求超时 500ms
            .setConnectionRequestTimeout(500)
            .build();

    // 初始化连接管理器
    static {
        final LayeredConnectionSocketFactory socketFactory;
        try {
            socketFactory = new SSLConnectionSocketFactory(SSLContext.getDefault());
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", socketFactory)
                    .register("http", new PlainConnectionSocketFactory())
                    .build();
            CONNECTION_MANAGER = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            CONNECTION_MANAGER.setMaxTotal(200);
            CONNECTION_MANAGER.setDefaultMaxPerRoute(20);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Connection Manager Initialize Error While Creating SSL Object.");
            e.printStackTrace();
        }
    }

    /**
     * 创建一个可关闭的Http客户端
     *
     * @param requestConfig 需要配置的Request配置参数
     * @return
     */
    private static CloseableHttpClient getHttpClient(RequestConfig requestConfig) {
        return HttpClients.custom()
                .setConnectionManager(CONNECTION_MANAGER)
                .setDefaultRequestConfig(requestConfig)
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                .build();
    }

    /**
     * 创建一个可关闭的Http客户端
     *
     * @return CloseableHttpClient
     */
    private static CloseableHttpClient getHttpClient() {
        return getHttpClient(REQUEST_CONFIG);
    }


    private static String postEntity(String uri, AbstractHttpEntity data) throws IOException {
        // 创建默认的httpClient实例
        final CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse httpResponse = null;
        try {
            final HttpPost postBase = new HttpPost(uri);
            postBase.setEntity(data);
            httpResponse = httpClient.execute(postBase);
            final HttpEntity entity = httpResponse.getEntity();
            if (null != entity) {
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    return EntityUtils.toString(entity);
                } else {
                    throw new IOException("Invalid HTTP status code:" + Integer.toString(statusCode));
                }
            } else {
                throw new IOException("Can't get HttpEntity");
            }
        } finally {
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                    httpResponse.close();
                } catch (IOException e) {
                    System.err.println("Failed to close HttpResponse:");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 使用POST提交数据
     *
     * @param url      URL
     * @param postData 需要提交的UTF-8格式的字符串
     * @return 服务端返回的结果
     * @throws Exception 提交过程中产生异常
     */
    public static String post(String url, String postData) throws Exception {
        return postEntity(url, new StringEntity(postData, "UTF-8"));
    }

    /**
     * 使用POST提交数据
     *
     * @param url      URL
     * @param postData 需要提交的UTF-8格式的字符串
     * @return 服务端返回的结果
     * @throws Exception 提交过程中产生异常
     */
    public static String post(String url, byte[] postData) throws Exception {
        return postEntity(url, new ByteArrayEntity(postData));
    }

    /**
     * 使用POST提交经过gZip压缩的数据
     *
     * @param url      URL
     * @param postData 需要提交的UTF-8格式的字符串
     * @return 服务端返回的结果
     * @throws Exception 提交过程中产生异常
     */
    public static String postCompressedData(String url, String postData) throws Exception {
        return postEntity(url, new ByteArrayEntity(CompressUtil.compressUTF8String(postData)));
    }

    /**
     * 自动重试提交数据
     *
     * @param url        URL
     * @param postData   需要提交的UTF-8格式的字符串
     * @param retryTimes 失败后重试次数
     * @return
     * @throws Exception
     */
    public static String retryPostCompressedData(String url, String postData, int retryTimes) throws Exception {
        for (int i = 0; i < retryTimes; i++) {
            try {
                return postCompressedData(url, postData);
            } catch (Exception ex) {
                System.err.println("Error while post compressed data, retrying...");
                ex.printStackTrace();
            }
        }
        return postCompressedData(url, postData);
    }

    /**
     * 自动重试提交数据
     *
     * @param url      URL
     * @param postData 需要提交的UTF-8格式的字符串
     * @return
     * @throws Exception
     */
    public static String retryPostCompressedData(String url, String postData) throws Exception {
        return retryPostCompressedData(url, postData, 2);
    }

    /**
     * 自动重试提交数据
     *
     * @param url        URL
     * @param postData   需要提交的UTF-8格式的字符串
     * @param retryTimes 失败后重试次数
     * @return
     * @throws Exception
     */
    public static String retryPost(String url, String postData, int retryTimes) throws Exception {
        for (int i = 0; i < retryTimes; i++) {
            try {
                return post(url, postData);
            } catch (Exception ex) {
                System.err.println("Error while post data, retrying...");
                ex.printStackTrace();
            }
        }
        return post(url, postData);
    }

    /**
     * 自动重试提交数据（一共3次，因为事不过三）
     *
     * @param url      URL
     * @param postData 需要提交的UTF-8格式的字符串
     * @return
     * @throws Exception
     */
    public static String retryPost(String url, String postData) throws Exception {
        return retryPost(url, postData, 2);
    }


    private static final String STATUS_FIELD = "status";

    /**
     * 智能识别大部分JSON格式的成功响应（通过status字段）
     *
     * @param response 反馈信息
     * @throws Exception 校验失败
     */
    public static void verifyResponse(String response) throws Exception {
        final Document responseDoc = Document.parse(response);
        if (responseDoc.containsKey(STATUS_FIELD)) {
            final Object statusObj = responseDoc.get(STATUS_FIELD);
            if (statusObj instanceof String) {
                final String statusString = (String) statusObj;
                if (!statusString.contains("success") && !"200".equals(statusString)) {
                    throw new Exception("Entity of status(String) may be fail:" + statusString+ "\nReturn message:" + response);
                }
            } else if (statusObj instanceof Number) {
                final Number statusNumber = (Number) statusObj;
                final double eps = Math.abs(statusNumber.doubleValue() - 200);
                if (eps > 1e-3) {
                    throw new Exception("Entity of status(Number) may be fail:" + statusObj.toString() + "\nReturn message:" + response);
                }
            } else if (statusObj instanceof Boolean) {
                if (!(Boolean) statusObj) {
                    throw new Exception("Entity of status(Boolean) may be fail:" + ((Boolean) statusObj).toString()+ "\nReturn message:" + response);
                }
            } else {
                throw new Exception("Entity of status(Any) can't be recognized:" + statusObj.toString()+ "\nReturn message:" + response);
            }
        } else {
            throw new Exception("The field `status` not found. \nReturn message:" + response);
        }
    }
}
