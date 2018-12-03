package com.github.tsingjyujing.lofka.appender.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.github.tsingjyujing.lofka.basic.BaseAsyncProcessor;
import com.github.tsingjyujing.lofka.basic.LoggerJsonAsyncAutoProcessor;
import com.github.tsingjyujing.lofka.util.Constants;
import org.bson.Document;

import java.util.Map;

/**
 * LogBack Http Appender
 * <p>
 * 将截获的日志发送到相应的HTTP接口中去
 *
 * @author yuanyifan
 */
public class HttpAsyncAppender extends AppenderBase<ILoggingEvent> {

    /**
     * 创建消息处理器
     * @return
     */
    private BaseAsyncProcessor<Map> getProcessor() {
        if (processor == null) {
            processor = new LoggerJsonAsyncAutoProcessor(
                    Constants.urlProcessing(
                            getTarget(),
                            Constants.INTERFACE_PUSH_BATCH
                    ),
                    Constants.urlProcessing(
                            getTarget(),
                            Constants.INTERFACE_PUSH_BATCH_ZIP
                    )
            );
            processor.setMaxBufferSize(getMaxBufferSize());
            processor.setSleepDuration(getInterval());
        }
        return processor;
    }

    private BaseAsyncProcessor<Map> processor = null;
    private final LoggingEventToJson loggingEventToJson = new LoggingEventToJson(new Document());

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    private String applicationName = "";

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    private String target = "";

    private int interval = 1000;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    private int maxBufferSize = 1000;


    @Override
    protected void append(ILoggingEvent eventObject) {
        final Document doc = loggingEventToJson.toDocument(eventObject);
        doc.append("app_name", getApplicationName());
        try {
            getProcessor().offerData(doc);
        } catch (Exception ex) {
            System.err.printf("Error while POSTing log to %s.\nLog detail:%s\n", getTarget(), doc.toJson());
            ex.printStackTrace();
        }
    }
}