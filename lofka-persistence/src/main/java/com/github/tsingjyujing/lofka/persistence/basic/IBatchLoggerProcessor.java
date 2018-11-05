package com.github.tsingjyujing.lofka.persistence.basic;

import org.bson.Document;

import java.util.Collection;

/**
 * 批量数据处理
 *
 * @author yuanyifan
 */
public interface IBatchLoggerProcessor {

    /**
     * Process (print or persistence it)
     *
     * @param logs log in LoggerJson format
     */
    void processLoggers(Collection<Document> logs);

}
