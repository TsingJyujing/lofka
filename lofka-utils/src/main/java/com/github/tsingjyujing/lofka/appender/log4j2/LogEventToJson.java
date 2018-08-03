package com.github.tsingjyujing.lofka.appender.log4j2;

import com.github.tsingjyujing.lofka.basic.IJsonConvert;
import com.github.tsingjyujing.lofka.basic.LoggerJson;
import com.github.tsingjyujing.lofka.util.DocumentUtil;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.bson.Document;

import java.util.Map;

/**
 * 格式化LogBack的日志事件
 *
 * @author yuanyifan
 */
public class LogEventToJson implements IJsonConvert<LogEvent> {

    private final Document attachInfo;

    public LogEventToJson(Document attachInfo) {
        this.attachInfo = attachInfo;
        this.attachInfo.put("type","LOGBACK");
    }

    /**
     * 将来源类型的转换为JSON
     *
     * @param rawData
     * @return
     */
    @Override
    public Document toDocument(LogEvent rawData) {
        Document result = new Document(attachInfo);
        if (rawData != null) {
            result.append(LoggerJson.TAG_TIMESTAMP, (double) rawData.getTimeMillis());
            result.append(LoggerJson.TAG_LEVEL, rawData.getLevel().toString().toUpperCase());
            final String logMessage = rawData.getMessage().getFormattedMessage();
            try {
                result.append(LoggerJson.TAG_MESSAGE, Document.parse(logMessage));
            } catch (Exception ex) {
                result.append(LoggerJson.TAG_MESSAGE, logMessage);
            }
            result.append(LoggerJson.TAG_THREAD, rawData.getThreadName());

            if (!result.containsKey(LoggerJson.TAG_APP_NAME)) {
                result.append(LoggerJson.TAG_APP_NAME, "log4j2-");
            }
            result.append(LoggerJson.TAG_LOGGER, rawData.getLoggerName());

            result.append(LoggerJson.TAG_LOCATION, DocumentUtil.formatStackTrace(rawData.getSource()));

            result.append(LoggerJson.TAG_MDC, formatMDCInfo(rawData.getContextMap()));
            ThrowableProxy throwableInformation = rawData.getThrownProxy();
            if (throwableInformation != null) {
                result.append(LoggerJson.TAG_THROWABLES, DocumentUtil.formatThrowable(throwableInformation.getThrowable()));
            }
        }
        return DocumentUtil.cleanDocument(result);
    }


    /**
     * 增加MDC信息
     *
     * @param mdcInfo MDC信息
     */
    protected Document formatMDCInfo(final Map<String, String> mdcInfo) {
        Document mdcProperties = new Document();
        if (mdcInfo != null && mdcInfo.size() > 0) {
            for (Map.Entry<String, String> entry : mdcInfo.entrySet()) {
                final String key = (entry.getKey().contains(".")) ? entry.getKey().toString()
                        .replaceAll("\\.", "_") : entry.getKey().toString();
                mdcProperties.append(key, entry.getValue());
            }
        }
        return mdcProperties;
    }
}
