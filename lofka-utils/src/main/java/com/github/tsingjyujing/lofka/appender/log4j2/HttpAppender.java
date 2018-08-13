package com.github.tsingjyujing.lofka.appender.log4j2;

import static com.github.tsingjyujing.lofka.util.NetUtil.retryPost;

import com.github.tsingjyujing.lofka.util.Constants;
import com.github.tsingjyujing.lofka.util.NetUtil;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.bson.Document;

@Plugin(name = "LofkaHttp", category = "Core", elementType = "appender", printObject = true)
public class HttpAppender extends AbstractAppender {


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

    protected HttpAppender(String name, String target, String applicationName) {
        super(name, null, null, true);
        setTarget(
                Constants.urlProcessing(
                        target,
                        Constants.INTERFACE_PUSH_SINGLE
                )
        );
        setApplicationName(applicationName);
    }

    private final LogEventToJson logEventToJson = new LogEventToJson(new Document());

    /**
     * Logs a LogEvent using whatever logic this Appender wishes to use. It is typically recommended to use a
     * bridge pattern not only for the benefits from decoupling an Appender from its implementation, but it is also
     * handy for sharing resources which may require some form of locking.
     *
     * @param event The LogEvent.
     */
    @Override
    public void append(LogEvent event) {
        final Document doc = logEventToJson.toDocument(event);
        doc.append("app_name", applicationName);
        try {
            NetUtil.verifyResponse(NetUtil.retryPost(getTarget(), doc.toJson()));
        } catch (Exception ex) {
            System.err.printf("Error while POSTing log to %s.\nLog detail:%s\n", getTarget(), doc.toJson());
            ex.printStackTrace();
        }
    }

    @PluginFactory
    public static HttpAppender createAppender(
            // @formatter:off
            @PluginAttribute("name") final String name,
            @PluginAttribute("target") final String target,
            @PluginAttribute("applicationName") final String applicationName
    ) {
        return new HttpAppender(
                name, target, applicationName
        );
    }
}
