package com.github.tsingjyujing.lofka.server.queue;

import com.cvnavi.lofka.basic.BaseAsyncProcessor;
import com.cvnavi.lofka.basic.LoggerJsonAsyncAutoProcessor;
import com.cvnavi.lofka.util.Constants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 日志转发服务
 *
 * @author yuanyifan
 */
public class AsyncRedirectService implements IMessageQueue {


    private final BaseAsyncProcessor<Map> loggerJsonAsyncProcessor;
    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncRedirectService.class);
    private final static Gson GSON = new Gson();

    /**
     * 转发服务器消息队列
     *
     * @param redirectTarget 重定向目标
     */
    public AsyncRedirectService(String redirectTarget) {
        loggerJsonAsyncProcessor = new LoggerJsonAsyncAutoProcessor(
                Constants.urlProcessing(
                        redirectTarget,
                        Constants.INTERFACE_PUSH_BATCH
                ),
                Constants.urlProcessing(
                        redirectTarget,
                        Constants.INTERFACE_PUSH_BATCH_ZIP
                )
        );
    }

    /**
     * 推送消息
     *
     * @param message 消息正文
     * @throws Exception 推送异常
     */
    @Override
    public void pushQueue(String message) throws Exception {
        Map data = GSON.fromJson(message, Map.class);
        if (data != null) {
            loggerJsonAsyncProcessor.offerData(data);
        } else {
            throw new RuntimeException("Get an invalid message: " + message);
        }
    }

}
