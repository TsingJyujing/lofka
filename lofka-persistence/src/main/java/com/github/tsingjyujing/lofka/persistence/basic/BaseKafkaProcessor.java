package com.github.tsingjyujing.lofka.persistence.basic;

import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Receive logs from kafka and call ILoggerProcessor to process it
 *
 * @author yuanyifan
 */
public abstract class BaseKafkaProcessor implements ILogReceiver {

    protected final static Logger LOGGER = LoggerFactory.getLogger(BaseKafkaProcessor.class);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Properties kafkaConfig;
    private final KafkaConsumer<Integer, String> consumer;

    /**
     * Initialize kafka connection
     * see example in test/resources/kafka-client.properties
     *
     * @param kafkaConfig kafka configuration
     */
    public BaseKafkaProcessor(Properties kafkaConfig) {
        this.kafkaConfig = kafkaConfig;

        consumer = new KafkaConsumer<>(kafkaConfig);

        // Shutdown gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                closed.set(true);
                consumer.wakeup();
            }
        }));
    }

    @Override
    public void run() {

        final String defaultTopic = kafkaConfig.getProperty("kafka.topic", "logger-json");

        consumer.subscribe(Lists.newArrayList(defaultTopic));

        LOGGER.info(
                "Listening from topic {}", defaultTopic
        );

        while (!closed.get()) {
            try {
                final ConsumerRecords<Integer, String> records = consumer.poll(Long.MAX_VALUE);

                final List<Document> buffer = Lists.newArrayList();
                for (ConsumerRecord<Integer, String> record : records) {
                    try {
                        Document doc = Document.parse(record.value());
                        // Append meta data in Kafka
                        doc.append(
                                "kafka_info", new Document(
                                        "offset", record.offset()
                                ).append(
                                        "partition", record.topic()
                                ).append(
                                        "topic", record.topic()
                                )
                        );

                        buffer.add(doc);
                        LOGGER.trace(
                                "Get message {}/{}/{}",
                                record.topic(),
                                record.partition(),
                                record.offset()
                        );
                    } catch (Throwable ex) {
                        LOGGER.error(
                                String.format(
                                        "Error while process batch data from Kafka, key/value: %s / %s",
                                        record.key(),
                                        record.value()
                                ),
                                ex
                        );
                    }
                }

                try {
                    processLoggers(buffer);
                } catch (Throwable ex) {
                    LOGGER.error("Error while process batch data from Kafka:", ex);
                }
                LOGGER.debug("Auto committing offset");
                try {
                    consumer.commitSync();
                } catch (CommitFailedException e) {
                    LOGGER.error("Error while commit sync", e);
                }
            } catch (Exception kafkaException) {
                LOGGER.error("Error while reading kafka", kafkaException);
            }
        }
    }

    /**
     * 获取配置信息
     *
     * @return
     */
    public Properties getProperties() {
        return kafkaConfig;
    }
}
