package com.github.tsingjyujing.lofka.appender.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.github.tsingjyujing.lofka.util.Constants;
import com.github.tsingjyujing.lofka.util.NetUtil;
import org.bson.Document;

import static com.github.tsingjyujing.lofka.util.NetUtil.retryPost;

/**
 * LogBack Http Appender
 *
 * 将截获的日志发送到相应的HTTP接口中去
 *
 * @author yuanyifan
 */
public class HttpAppender extends AppenderBase<ILoggingEvent> {

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

    @Override
    protected void append(ILoggingEvent eventObject) {
        final Document doc = loggingEventToJson.toDocument(eventObject);
        doc.append("app_name", getApplicationName());
        try {
            NetUtil.verifyResponse(NetUtil.retryPost(Constants.urlProcessing(
                    getTarget(),
                    Constants.INTERFACE_PUSH_SINGLE
            ), doc.toJson()));
        } catch (Exception ex) {
            System.err.printf("Error while POSTing log to %s.\nLog detail:%s\n", getTarget(), doc.toJson());
            ex.printStackTrace();
        }
    }
}