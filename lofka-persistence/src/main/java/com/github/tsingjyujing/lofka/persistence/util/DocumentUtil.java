package com.github.tsingjyujing.lofka.persistence.util;

import com.github.tsingjyujing.lofka.basic.LoggerJson;
import com.google.gson.Gson;
import org.bson.Document;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class DocumentUtil {

    private static final Gson GSON = new Gson();

    private static final Document EXPIRED_SETTING = new Document(
            "TRACE", 1000L * 5 * 60
    ).append(
            "DEBUG", 1000L * 10 * 60
    ).append(
            "INFO", 1000L * 24 * 60 * 60
    ).append(
            "WARN", 1000L * 31 * 24 * 60 * 60
    ).append(
            "ERROR", 1000L * 366 * 24 * 60 * 60
    ).append(
            "FATAL", 1000L * 732 * 24 * 60 * 60
    ).append(
            "DEFAULT", 1000L * 31 * 24 * 60 * 60
    ).append(
            "NGINX", 1000L * 190 * 24 * 60 * 60
    );

    /**
     * MongoDB 日志处理工具
     * <p>
     * 1. 时间戳转换为Date类型
     * 2. 写入时间追加
     * 3. 根据等级和Type确定超时时间
     * 4. 以`[` 和 `{` 开头的消息体尝试转换为JSON
     *
     * @param rawData
     * @return
     */
    public static Document mongoLogProcessor(Document rawData, Document expiredSetting) {
        Document doc = new Document(rawData);
        // 时间戳转换为Date类型
        Double logTime = doc.getDouble(LoggerJson.TAG_TIMESTAMP);
        if (doc.containsKey(LoggerJson.TAG_TIMESTAMP)) {
            doc.append(LoggerJson.TAG_TIMESTAMP, new Date(logTime.longValue()));
        }
        doc.append("write_timestamp", new Date(System.currentTimeMillis()));
        String level = null;
        if (doc.containsKey(LoggerJson.TAG_LEVEL)) {
            level = doc.getString(LoggerJson.TAG_LEVEL).toUpperCase();
            doc.append(LoggerJson.TAG_LEVEL, level);
        }
        String logType = null;
        if (doc.containsKey("type")) {
            logType = doc.getString("type").toUpperCase();
            doc.append("type", logType);
        } else {
            logType = "NO_TYPE";
            doc.append("type", "NO_TYPE");
        }
        double expiredMills = expiredSetting.getDouble("DEFAULT");
        if (expiredSetting.containsKey(level)) {
            expiredMills = expiredSetting.getDouble(level);
        } else if (expiredSetting.containsKey(logType)) {
            expiredMills = expiredSetting.getDouble(logType);
        }
        doc.append("expired_time", new Date((long) (logTime + expiredMills)));
        return doc;
    }

    /**
     * @param properties
     * @param prefix
     * @return
     */
    public static Document getExpiredSetting(Properties properties, String prefix) {
        final Document doc = new Document();
        final String prefixWithDot = prefix + ".";
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String key = entry.getKey().toString();
            if (key.startsWith(prefixWithDot)) {
                doc.append(
                        key.substring(prefixWithDot.length()),
                        Double.valueOf(entry.getValue().toString())
                );
            }
        }
        return doc;
    }
}
