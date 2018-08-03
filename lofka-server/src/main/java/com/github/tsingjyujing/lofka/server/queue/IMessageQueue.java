package com.github.tsingjyujing.lofka.server.queue;

/**
 * 将消息推送到队列，目前只支持Kafka，以后可能要支持各种奇葩框架
 *
 * @author yuanyifan
 */
public interface IMessageQueue {
    /**
     * 推送消息
     *
     * @param message 消息正文
     * @throws Exception 推送异常
     */
    void pushQueue(String message) throws Exception;
}
