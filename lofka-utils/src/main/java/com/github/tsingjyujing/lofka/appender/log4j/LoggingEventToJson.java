package com.github.tsingjyujing.lofka.appender.log4j;

import com.github.tsingjyujing.lofka.basic.IJsonConvert;
import com.github.tsingjyujing.lofka.basic.LoggerJson;
import com.github.tsingjyujing.lofka.util.DocumentUtil;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.bson.Document;

import java.util.Map;

/**
 * 格式化Log4j 1.x 的日志事件
 *
 * @author yuanyifan
 */
public class LoggingEventToJson implements IJsonConvert<LoggingEvent> {

    private final Document attachInfo;

    public LoggingEventToJson(Document attachInfo) {
        this.attachInfo = attachInfo;
        this.attachInfo.put("type","LOG4J1X");
    }

    @Override
    public Document toDocument(LoggingEvent rawData) {
        Document result = new Document(attachInfo);

        if (rawData != null) {

            result.append(LoggerJson.TAG_TIMESTAMP, (double) rawData.getTimeStamp());
            result.append(LoggerJson.TAG_LEVEL, rawData.getLevel().toString().toUpperCase());
            final String logMessage = rawData.getRenderedMessage();
            try {
                result.append(LoggerJson.TAG_MESSAGE, Document.parse(logMessage));
            } catch (Exception ex) {
                result.append(LoggerJson.TAG_MESSAGE, logMessage);
            }
            result.append(LoggerJson.TAG_THREAD, rawData.getThreadName());
            result.append(LoggerJson.TAG_LOGGER, rawData.getLoggerName());
            result.append(LoggerJson.TAG_LOCATION, formatLocationInfo(rawData.getLocationInformation()));
            result.append(LoggerJson.TAG_MDC, formatMDCInfo(rawData.getProperties()));
            ThrowableInformation throwableInformation = rawData.getThrowableInformation();
            if (throwableInformation != null) {
                if (throwableInformation.getThrowable() != null) {
                    result.append(LoggerJson.TAG_THROWABLES, DocumentUtil.formatThrowable(throwableInformation.getThrowable()));
                }
            }
            result.append("ndc", rawData.getNDC());
            if (rawData.getProperties().containsKey("application")) {
                result.append(LoggerJson.TAG_APP_NAME, rawData.getProperties().get("application"));
            } else {
                result.append(LoggerJson.TAG_APP_NAME, "log4j_socket");
            }
        }
        return DocumentUtil.cleanDocument(result);
    }


    /**
     * 增加MDC信息
     *
     * @param mdcInfo MDC信息
     */
    protected Document formatMDCInfo(final Map<Object, Object> mdcInfo) {
        Document mdcProperties = new Document();
        if (mdcInfo != null && mdcInfo.size() > 0) {
            for (Map.Entry<Object, Object> entry : mdcInfo.entrySet()) {
                final String key = (entry.getKey().toString().contains(".")) ? entry.getKey().toString()
                        .replaceAll("\\.", "_") : entry.getKey().toString();
                mdcProperties.append(key, entry.getValue().toString());
            }
        }
        return mdcProperties;
    }

    /**
     * 格式化Location信息
     *
     * @param locationInfo
     */
    protected Document formatLocationInfo(final LocationInfo locationInfo) {
        Document doc = new Document();
        if (locationInfo != null) {
            doc.append(LoggerJson.TAG_LOCATION_FILENAME, locationInfo.getFileName());
            doc.append(LoggerJson.TAG_LOCATION_METHOD, locationInfo.getMethodName());
            doc.append(LoggerJson.TAG_LOCATION_LINE, locationInfo.getLineNumber());
            doc.append(LoggerJson.TAG_LOCATION_CLASS, locationInfo.getClassName());
        }
        return doc;
    }

}
