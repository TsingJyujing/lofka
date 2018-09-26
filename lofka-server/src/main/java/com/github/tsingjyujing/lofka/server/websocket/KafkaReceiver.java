package com.github.tsingjyujing.lofka.server.websocket;

import com.github.tsingjyujing.lofka.persistence.basic.BaseKafkaProcessor;
import org.bson.Document;

import java.util.Properties;

/**
 * 接受Kafka数据，并且传递给WebSocket的进程
 *
 * @author yuanyifan
 */
public class KafkaReceiver extends BaseKafkaProcessor {

    /**
     * Kafka数据接收器
     */
    public KafkaReceiver(Properties properties) {
        super(properties);

        LOGGER.info("Start kafka receiver (to websocket) successfully.");
    }

    /**
     * 处理批量的日志数据
     *
     * @param logs 日志数据
     * @throws Exception
     */
    @Override
    public void processLoggers(Iterable<Document> logs) throws Exception {
        Exception lastEx = null;
        for (Document log : logs) {
            try {
                LoggerPushWebSocket.sendMessageGrouply(log.toJson());
            } catch (Exception ex) {
                lastEx = ex;
            }
        }
        if (lastEx != null) {
            throw lastEx;
        }
    }

}
