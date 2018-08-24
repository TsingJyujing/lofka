package com.github.tsingjyujing.lofka.util;

import com.github.tsingjyujing.lofka.basic.LoggerJson;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * 文档生成工具类
 *
 * @author yuanyifan
 */
public class DocumentUtil {
    private final static Gson GSON = new Gson();

    /**
     * 格式化错误调试堆栈信息
     *
     * @param stackTraceElement
     * @return
     */
    public static Document formatStackTrace(StackTraceElement stackTraceElement) {
        Document doc = new Document();
        if (stackTraceElement != null) {
            doc.append(LoggerJson.TAG_THROWABLES_STACK_TRACE_CLASS, stackTraceElement.getClassName());
            doc.append(LoggerJson.TAG_THROWABLES_STACK_TRACE_FILENAME, stackTraceElement.getFileName());
            doc.append(LoggerJson.TAG_THROWABLES_STACK_TRACE_METHOD, stackTraceElement.getMethodName());
            doc.append(LoggerJson.TAG_THROWABLES_STACK_TRACE_LINE, stackTraceElement.getLineNumber());
        }
        return doc;
    }

    /**
     * 对结果进行清理
     *
     * @param rawDoc
     * @return
     */
    public static Document cleanDocument(Document rawDoc) {
        Document doc = new Document();
        for (Map.Entry<String, Object> entryUnit : rawDoc.entrySet()) {
            final Object value = entryUnit.getValue();
            if (value != null) {
                if (value instanceof Document) {
                    doc.append(entryUnit.getKey(), cleanDocument((Document) value));
                } else {
                    doc.append(entryUnit.getKey(), value);
                }
            }
        }
        return doc;
    }

    /**
     * 格式化Location信息
     *
     * @param throwable
     */
    public static Document formatThrowable(final Throwable throwable) {
        Document doc = new Document();
        if (throwable != null) {
            doc.append(LoggerJson.TAG_THROWABLES_MESSAGE, throwable.getMessage());
            List<Document> stackTraceInfo = Lists.newArrayList();
            for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                stackTraceInfo.add(DocumentUtil.formatStackTrace(stackTraceElement));
            }
            doc.append(LoggerJson.TAG_THROWABLES_STACK_TRACE, stackTraceInfo);
        }
        return doc;
    }

    /**
     * 生成心跳包数据
     *
     * @return
     */
    public static String generateHeartBeatMessage(String name, long interval) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("name", name);
        data.put("interval", interval);
        data.put("type", "heartbeat");
        return GSON.toJson(data);
    }

}
