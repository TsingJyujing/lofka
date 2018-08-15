package com.github.tsingjyujing.lofka.appender.log4j;

import com.github.tsingjyujing.lofka.basic.IJsonConvert;
import com.github.tsingjyujing.lofka.util.Constants;
import com.github.tsingjyujing.lofka.util.NetUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.bson.Document;

import static com.github.tsingjyujing.lofka.util.NetUtil.retryPost;

/**
 * Http日志发送器，利用HTTP的高性能传输日志
 * Deprecated since 1.6
 * 在没有特别变态的需求下请选择使用异步发送器
 */
public class HttpAppender extends AppenderSkeleton {

    private final IJsonConvert<LoggingEvent> jsonConvertible;

    private String application = null;
    private String target = null;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


    public HttpAppender() {
        jsonConvertible = new LoggingEventToJson(
                new Document()
        );
    }


    /**
     * Subclasses of <code>AppenderSkeleton</code> should implement this
     * method to perform actual logging. See also {@link #doAppend
     * AppenderSkeleton.doAppend} method.
     *
     * @param event
     * @since 0.9.0
     */
    @Override
    protected void append(LoggingEvent event) {
        try {
            final Document doc = jsonConvertible.toDocument(
                    event
            );
            if (application != null) {
                doc.append("app_name", application);
            }
            NetUtil.verifyResponse(NetUtil.retryPost(Constants.urlProcessing(
                    getTarget(),
                    Constants.INTERFACE_PUSH_SINGLE
            ), doc.toJson()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Release any resources allocated within the appender such as file
     * handles, network connections, etc.
     *
     * <p>It is a programming error to append to a closed appender.
     *
     * @since 0.8.4
     */
    @Override
    public void close() {
    }

    /**
     * Configurators call this method to determine if the appender
     * requires a layout. If this method returns <code>true</code>,
     * meaning that layout is required, then the configurator will
     * configure an layout using the configuration information at its
     * disposal.  If this method returns <code>false</code>, meaning that
     * a layout is not required, then layout configuration will be
     * skipped even if there is available layout configuration
     * information at the disposal of the configurator..
     *
     * <p>In the rather exceptional case, where the appender
     * implementation admits a layout but can also work without it, then
     * the appender should return <code>true</code>.
     *
     * @since 0.8.4
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }
}
