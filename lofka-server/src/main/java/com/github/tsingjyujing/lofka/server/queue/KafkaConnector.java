package com.github.tsingjyujing.lofka.server.queue;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Kafka 消息队列连接器
 *
 * @author yuanyifan
 */
public class KafkaConnector implements IMessageQueue {


    public KafkaProducer<Integer, String> getProducer() {
        return producer;
    }

    private KafkaProducer<Integer, String> producer;

    /**
     * 读取什么样的Topic
     * 这里设置的是默认值
     * 真实值由`logger.topic`决定
     */
    private String topicWrite = "logger-json";

    private final Random random = new Random(System.currentTimeMillis());

    /**
     * 初始化Kafka连接
     *
     * @param properties 配置内容
     */
    public KafkaConnector(Properties properties) {
        producer = createProducer(properties);
        topicWrite = properties.getProperty("logger.topic", topicWrite);
    }


    /**
     * 通过配置创建生产者
     *
     * @param properties 配置
     * @return 生产者对象
     */
    public static KafkaProducer<Integer, String> createProducer(Properties properties) {
        return new KafkaProducer<>(properties);
    }

    @Override
    public void pushQueue(String message) throws Exception {
        pushMessageToTopicAsync(topicWrite, message);
    }

    /**
     * 直接向指定的Topic推送消息
     *
     * @param topic   写入哪个主题
     * @param message 消息详情
     */
    public Future<RecordMetadata> pushMessageToTopicAsync(String topic, String message) {
        return getProducer().send(
                new ProducerRecord<>(
                        topic,
                        random.nextInt(),
                        message
                )
        );
    }

    /**
     * 直接向指定的Topic推送消息
     *
     * @param topic
     * @param message
     */
    public RecordMetadata pushMessageToTopic(String topic, String message) throws ExecutionException, InterruptedException {
        return producer.send(
                new ProducerRecord<>(
                        topic,
                        random.nextInt(),
                        message
                )
        ).get();
    }
}
