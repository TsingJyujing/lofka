package com.github.tsingjyujing.lofka.appender.log4j2;

import com.github.tsingjyujing.lofka.basic.BaseAsyncProcessor;
import com.github.tsingjyujing.lofka.basic.LoggerJsonAsyncAutoProcessor;
import com.github.tsingjyujing.lofka.util.Constants;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.bson.Document;

import java.util.Map;

/**
 * Log4J2 的Http异步提交工具
 */
@Plugin(name = "LofkaAsyncHttp", category = "Core", elementType = "appender", printObject = true)
public class HttpAsyncAppender extends AbstractAppender {

    private final BaseAsyncProcessor<Map> processor;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    private String applicationName = "";


    /**
     * 初始化Appender
     *
     * @param name            Appender名称
     * @param target          推送目标
     * @param applicationName 应用名称
     * @param interval        推送间隔
     * @param maxBufferSize   最大缓冲区大小
     */
    protected HttpAsyncAppender(String name, String target, String applicationName, int interval, int maxBufferSize) {
        super(name, null, null, true);
        setApplicationName(applicationName);
        processor = new LoggerJsonAsyncAutoProcessor(
                Constants.urlProcessing(
                        target,
                        Constants.INTERFACE_PUSH_BATCH
                ),
                Constants.urlProcessing(
                        target,
                        Constants.INTERFACE_PUSH_BATCH_ZIP
                )
        );
        processor.setSleepDuration(interval);
        processor.setMaxBufferSize(maxBufferSize);
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
        doc.append("app_name", getApplicationName());
        processor.offerData(doc);
    }

    /**
     * Appender 插件工厂方法
     *
     * @param name            Appender名称
     * @param target          推送目标
     * @param applicationName 应用名称
     * @param interval        推送间隔
     * @param maxBufferSize   最大缓冲区大小
     * @return
     */
    @PluginFactory
    public static HttpAsyncAppender createAppender(
            // @formatter:off
            @PluginAttribute("name") final String name,
            @PluginAttribute("target") final String target,
            @PluginAttribute("applicationName") final String applicationName,
            @PluginAttribute(value = "interval", defaultInt = 1000) final int interval,
            @PluginAttribute(value = "maxBufferSize", defaultInt = 1000) final int maxBufferSize
    ) {
        return new HttpAsyncAppender(
                name,
                target,
                applicationName,
                interval,
                maxBufferSize
        );
    }
}
