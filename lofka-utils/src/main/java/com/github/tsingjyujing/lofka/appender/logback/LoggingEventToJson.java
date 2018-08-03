package com.github.tsingjyujing.lofka.appender.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.github.tsingjyujing.lofka.basic.IJsonConvert;
import com.github.tsingjyujing.lofka.basic.LoggerJson;
import com.github.tsingjyujing.lofka.util.DocumentUtil;
import com.google.common.collect.Lists;
import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * 格式化LogBack的日志事件
 *
 * @author yuanyifan
 */
public class LoggingEventToJson implements IJsonConvert<ILoggingEvent> {

    private final Document attachInfo;

    public LoggingEventToJson(Document attachInfo) {
        this.attachInfo = attachInfo;
        this.attachInfo.put("type","LOG4J2X");
    }

    private static final String LOGGER_NAME_SPLITOR = "://";

    @Override
    public Document toDocument(ILoggingEvent rawData) {
        Document result = new Document(attachInfo);
        if (rawData != null) {
            result.append(LoggerJson.TAG_TIMESTAMP, (double) rawData.getTimeStamp());
            result.append(LoggerJson.TAG_LEVEL, rawData.getLevel().toString().toUpperCase());
            final String logMessage = rawData.getFormattedMessage();
            try {
                result.append(LoggerJson.TAG_MESSAGE, Document.parse(logMessage));
            } catch (Exception ex) {
                result.append(LoggerJson.TAG_MESSAGE, logMessage);
            }
            result.append(LoggerJson.TAG_THREAD, rawData.getThreadName());

            if (rawData.getLoggerName().contains(LOGGER_NAME_SPLITOR)) {
                String[] splitedLoggerName = rawData.getLoggerName().split(LOGGER_NAME_SPLITOR);
                result.append(LoggerJson.TAG_APP_NAME, splitedLoggerName[0]);
                result.append(LoggerJson.TAG_LOGGER, splitedLoggerName[1]);
            } else {
                result.append(LoggerJson.TAG_APP_NAME, "logback_socket");
                result.append(LoggerJson.TAG_LOGGER, rawData.getLoggerName());
            }

            /**
             * 日志产生处Location信息
             */
            if (rawData.hasCallerData()) {
                if (rawData.getCallerData().length > 0) {
                    result.append(LoggerJson.TAG_LOCATION, DocumentUtil.formatStackTrace(rawData.getCallerData()[0]));
                }

            }

            /**
             * 日志产生处调用堆栈信息
             */
            final List<Document> stackTraceInfo = Lists.newArrayList();
            for (StackTraceElement stackTraceElement : rawData.getCallerData()) {
                stackTraceInfo.add(DocumentUtil.formatStackTrace(stackTraceElement));
            }
            if (rawData.getCallerData().length > 0) {
                result.append(LoggerJson.TAG_THROWABLES_STACK_TRACE, stackTraceInfo);
            }

            result.append(LoggerJson.TAG_MDC, formatMDCInfo(rawData.getMDCPropertyMap()));
            IThrowableProxy throwableInformation = rawData.getThrowableProxy();
            if (throwableInformation != null) {
                result.append(LoggerJson.TAG_THROWABLES, formatThorwable(throwableInformation));

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

    /**
     * 格式化Location信息
     *
     * @param throwable
     */
    protected Document formatThorwable(final IThrowableProxy throwable) {
        Document doc = new Document();
        if (throwable != null) {
            doc.append(LoggerJson.TAG_THROWABLES_MESSAGE, throwable.getMessage());
            List<Document> stackTraceInfo = Lists.newArrayList();
            for (StackTraceElementProxy stackTraceElement : throwable.getStackTraceElementProxyArray()) {
                stackTraceInfo.add(DocumentUtil.formatStackTrace(stackTraceElement.getStackTraceElement()));
            }
            doc.append(LoggerJson.TAG_THROWABLES_STACK_TRACE, stackTraceInfo);
        }
        return doc;
    }

}
