package com.github.tsingjyujing.lofka.server.websocket;

import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 接受Kafka数据的进程
 * @author yuanyifan
 */
public class KafkaReceiver implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaReceiver.class);
    private final KafkaConsumer<Integer, String> consumer;
    private final Properties properties;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Kafka数据接收器
     */
    public KafkaReceiver(Properties properties) {
        // 读取配置文件初始化 Kafka Client
        // 准备循环读取
        consumer = new KafkaConsumer<>(properties);
        this.properties = properties;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                closed.set(true);
                consumer.wakeup();
            }
        }));
        // 设置新的GroupId，同时不提交Offset到Zookeeper
        this.properties.put(
                "group.id",
                String.format(
                        "logger-json-server-consumer-%d",
                        System.currentTimeMillis()
                )
        );
    }

    @Override
    public void run() {
        final String defaultTopic = properties.getProperty("kafka.topic", "logger-json");
        consumer.subscribe(Lists.newArrayList(defaultTopic));
        LOGGER.debug("Reading topic:\t" + defaultTopic);
        while (!closed.get()) {
            try {
                LOGGER.debug("Polling...");
                final ConsumerRecords<Integer, String> records = consumer.poll(10000);
                LOGGER.debug("Polled, interring...");
                for (ConsumerRecord<Integer, String> record : records) {
                    try {
                        // 推送到群发接口
                        LoggerPushWebSocket.sendMessageGrouply(record.value());
                        LOGGER.debug(
                                "Get message {}/{}/{}",
                                record.topic(),
                                record.partition(),
                                record.offset()
                        );
                    } catch (Throwable ex) {
                        LOGGER.error("Error while process data from Kafka.", ex);
                        LOGGER.error("Message value: {}", record.value());
                        LOGGER.error("Message key: {}", record.key());
                    }
                }
            } catch (Exception kafkaException) {
                LOGGER.error("Error while reading kafka", kafkaException);
            }
        }
        consumer.close();
    }
}
