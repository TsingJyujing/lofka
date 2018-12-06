package com.github.tsingjyujing.lofka.nightwatcher.engine.tool;

import com.github.tsingjyujing.lofka.util.FileUtil;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tsingjyujing.lofka.util.NetUtil.httpRequest;

/**
 * JS引擎使用的工具链
 * 这里所有的方法必须保证线程安全
 * JS中可以访问`__ENGINE_TOOLS`调用（不推荐直接调用）
 */
public class CommonTools {

    /**
     * 提交GET请求
     *
     * @param uri  需要请求的URI
     * @param data JSON格式的KV数据，将会以URL的格式编码
     * @return
     * @throws Exception
     */
    public static String get(String uri, String data) throws Exception {
        final Document kvs = Document.parse(data);
        final URIBuilder builder = new URIBuilder(uri);
        kvs.forEach((k, v) -> builder.setParameter(k, v.toString()));
        return httpRequest(new HttpGet(builder.build()));
    }


    /**
     * 提交POST请求
     *
     * @param uri  需要请求的URI
     * @param data JSON格式的KV数据，将会以URL的格式编码
     * @return
     * @throws Exception
     */
    public static String postUrlencoded(String uri, String data) throws Exception {
        final List<NameValuePair> postForm = Document.parse(data).entrySet().stream().map(
                entry -> new BasicNameValuePair(
                        entry.getKey(),
                        entry.getValue().toString()
                )
        ).collect(
                Collectors.toList()
        );
        return post(
                uri,
                new UrlEncodedFormEntity(postForm, Consts.UTF_8),
                "application/x-www-form-urlencoded;charset=utf-8"
        );
    }


    /**
     * 通用POST
     *
     * @param uri         需要请求的URI
     * @param entity      实体
     * @param contentType 正文类型
     * @return
     * @throws Exception
     */
    private static String post(String uri, AbstractHttpEntity entity, String contentType) throws Exception {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", contentType);
        return httpRequest(httpPost);
    }

    /**
     * 提交POST请求
     *
     * @param uri  需要请求的URI
     * @param data 字符串数据
     * @return
     * @throws Exception
     */
    public static String postString(String uri, String data, String contentType) throws Exception {
        return post(
                uri,
                new StringEntity(data),
                contentType
        );
    }

    /**
     * 创建一个Logger用于日志
     *
     * @param loggerName Logger的标识
     * @return
     */
    public static Logger createLogger(String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }

    private final static Gson GSON = new Gson();

    /**
     * 转换为JSON
     *
     * @param anyObject 任意对象
     * @return JSON字符串
     */
    public static String toJson(Object anyObject) {
        return GSON.toJson(anyObject);
    }

    /**
     * JSON->Document
     *
     * @param json JSON字符串
     * @return Document对象
     */
    public static Document fromJson(String json) {
        return Document.parse(json);
    }

    /**
     * JSON->Document
     *
     * @param json JSON字符串
     * @return Document对象
     */
    public static Map<String, String> stringMapFromJson(String json) {
        final Map<String, String> map = Maps.newHashMap();
        fromJson(json).forEach((k, v) -> map.put(k, v.toString()));
        return map;
    }


    /**
     * 从资源中加载文件
     *
     * @param filename 文件名
     * @return
     * @throws IOException
     */
    public static String loadResourceFile(String filename) throws IOException {
        return FileUtil.readTextResource(filename);
    }
}
