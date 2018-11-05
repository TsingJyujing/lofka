package com.github.tsingjyujing.lofka.persistence.source;

import com.github.tsingjyujing.lofka.persistence.basic.BaseKafkaProcessor;
import com.github.tsingjyujing.lofka.persistence.basic.IBatchLoggerProcessor;
import com.github.tsingjyujing.lofka.persistence.basic.ILogReceiverProcessable;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * Kafka消费之后持久化到多处
 *
 * @author yuanyifan
 */
public class KafkaMultiPersistence extends BaseKafkaProcessor implements ILogReceiverProcessable {


    private final ArrayList<IBatchLoggerProcessor> processors;

    /**
     * Initialize kafka connection
     * see example in test/resources/kafka-client.properties
     *
     * @param kafkaConfig kafka configuration
     */
    public KafkaMultiPersistence(Properties kafkaConfig, Iterable<IBatchLoggerProcessor> processors) {
        super(kafkaConfig);
        this.processors = Lists.newArrayList(processors);
    }

    /**
     * 获取所有的处理器
     *
     * @return
     */
    @Override
    public Collection<IBatchLoggerProcessor> getProcessors() {
        return processors;
    }
}
