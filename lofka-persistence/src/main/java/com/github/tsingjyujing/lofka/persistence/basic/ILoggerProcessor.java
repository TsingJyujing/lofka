package com.github.tsingjyujing.lofka.persistence.basic;

import org.bson.Document;

/**
 * Logger processor
 *
 * @author yuanyifan
 */
public interface ILoggerProcessor extends IBatchLoggerProcessor {

    /**
     * Process (print or persistence it)
     * <p>
     * NOTICE: To handle exceptions in function
     *
     * @param log log in LoggerJson format
     */
    void processLogger(Document log);

    @Override
    default void processLoggers(Iterable<Document> logs) {
        for (Document log : logs) {
            processLogger(log);
        }
    }
}
