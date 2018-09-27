package com.github.tsingjyujing.lofka.persistence.writers;

import com.github.tsingjyujing.lofka.persistence.basic.IBatchLoggerProcessor;
import org.bson.Document;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * 日志消息写入文件
 */
public class LocalFileWriter implements IBatchLoggerProcessor {

    private final PrintWriter writer;
    private final long flushInterval;
    private long lastFlush;

    /**
     * 初始化写入工具
     *
     * @param properties 属性
     * @throws FileNotFoundException 找不到文件
     */
    public LocalFileWriter(Properties properties) throws IOException {
        writer = new PrintWriter(new FileWriter(
                properties.getProperty("filename", "lofka.data.log"),
                Boolean.parseBoolean(
                        properties.getProperty("append", "true")
                )
        ));
        flushInterval = Long.parseLong(properties.getProperty("flush.interval", "5000"));
        resetLastFlush();
    }

    private long getTimeoutTick() {
        return flushInterval + lastFlush;
    }

    private boolean isTimeout() {
        return System.currentTimeMillis() >= getTimeoutTick();
    }

    private void resetLastFlush() {
        lastFlush = System.currentTimeMillis();
    }

    /**
     * Process (print or persistence it)
     *
     * @param logs log in LoggerJson format
     */
    @Override
    public void processLoggers(Iterable<Document> logs) {
        for (Document log : logs) {
            writer.println(log.toJson());
        }
        if (flushInterval <= 0) {
            writer.flush();
        } else if (isTimeout()) {
            writer.flush();
            resetLastFlush();
        }
    }
}
