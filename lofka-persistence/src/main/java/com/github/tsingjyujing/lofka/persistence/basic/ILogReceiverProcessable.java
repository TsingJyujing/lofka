package com.github.tsingjyujing.lofka.persistence.basic;

import org.bson.Document;

/**
 * 可处理批量日志的接收器
 */
public interface ILogReceiverProcessable extends ILogReceiver {

    /**
     * 获取所有的处理器
     *
     * @return
     */
    Iterable<IBatchLoggerProcessor> getProcessors();

    /**
     * 处理批量的日志数据
     *
     * @param logs 日志数据
     * @throws Exception
     */
    default void processLoggers(Iterable<Document> logs) throws Exception {
        for (IBatchLoggerProcessor processor : getProcessors()) {
            processor.processLoggers(logs);
        }
    }
}
