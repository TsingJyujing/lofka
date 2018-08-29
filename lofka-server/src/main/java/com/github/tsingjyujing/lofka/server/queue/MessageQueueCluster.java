package com.github.tsingjyujing.lofka.server.queue;

import com.github.tsingjyujing.lofka.server.util.Constants;
import com.github.tsingjyujing.lofka.util.FileUtil;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * 消息队列集群，所有的消息队列
 *
 * @author yuanyifan
 */
public class MessageQueueCluster implements IMessageQueue {
    private final static Logger LOGGER = LoggerFactory.getLogger(MessageQueueCluster.class);

    private static MessageQueueCluster ourInstance = new MessageQueueCluster();

    public static MessageQueueCluster getInstance() {
        return ourInstance;
    }

    private final Map<String, IMessageQueue> messageQueueMap = Maps.newHashMap();

    private MessageQueueCluster() {
        // 尝试通过配置初始化Kafka队列
        // 配置文件：lofka-kafka.properties
        try {
            final Properties kafkaConfig = FileUtil.autoReadProperties(Constants.FILE_LOFKA_QUEUE_KAFKA);
            messageQueueMap.put("kafka", new KafkaConnector(kafkaConfig));
        } catch (Exception ex) {
            LOGGER.info("Fail to load kafka Queue, may be not configured.");
        }
        // 尝试通过配置初始化转发队列
        // 配置文件：lofka-redirect.properties
        try {
            final Properties redirectConfig = FileUtil.autoReadProperties(Constants.FILE_LOFKA_QUEUE_REDIR);
            messageQueueMap.put("redirect", new AsyncRedirectService(redirectConfig.getProperty("redirect.target")));
        } catch (Exception ex) {
            LOGGER.info("Fail to load redirect Queue, may be not configured.");
        }
        // 如果有其他的转发配置也在这里
    }

    /**
     * 获取重定向队列
     *
     * @return 重定向队列
     * @throws Exception 无法获取或者异常
     */
    public AsyncRedirectService getRedirectConnector() throws Exception {
        return (AsyncRedirectService) getMessageQueue("redirect");
    }

    /**
     * 获取Kafka队列
     *
     * @return Kafka队列
     * @throws Exception 无法获取或者异常
     */
    public KafkaConnector getKafkaConnector() throws Exception {
        return (KafkaConnector) getMessageQueue("kafka");
    }

    /**
     * 获取指定名称的队列
     *
     * @return 队列
     * @throws Exception 无法获取或者异常
     */
    public IMessageQueue getMessageQueue(String name) throws Exception {
        if (messageQueueMap.containsKey(name)) {
            return messageQueueMap.get(name);
        } else {
            throw new Exception(String.format("MessageQueue %s not found.", name));
        }
    }


    /**
     * 推送所有的消息
     *
     * @param message 消息正文
     * @throws Exception 推送异常
     */
    @Override
    public void pushQueue(String message) throws Exception {
        for (IMessageQueue messageQueue : messageQueueMap.values()) {
            try {
                messageQueue.pushQueue(message);
            } catch (Exception ex) {
                LOGGER.warn(String.format("Fail to push message to %s : %s", messageQueue.getClass().toString(), message), ex);
            }
        }
    }
}
