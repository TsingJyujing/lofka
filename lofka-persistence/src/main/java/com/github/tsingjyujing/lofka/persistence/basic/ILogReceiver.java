package com.github.tsingjyujing.lofka.persistence.basic;

import org.bson.Document;

import java.util.Collection;

/**
 * 可处理批量日志数据的线程
 */
public interface ILogReceiver extends Runnable {

    /**
     * 处理批量的日志数据
     *
     * @param logs 日志数据
     * @throws Exception
     */
    void processLoggers(Collection<Document> logs) throws Exception;
}
