package com.github.tsingjyujing.lofka.persistence.source;

import com.github.tsingjyujing.lofka.basic.BaseAsyncProcessor;
import com.github.tsingjyujing.lofka.persistence.basic.IBatchLoggerProcessor;
import com.github.tsingjyujing.lofka.persistence.basic.ILogReceiverProcessable;
import com.google.common.collect.Lists;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 将服务器收到的数据进行处理
 */
public class LocalWriterQueue extends BaseAsyncProcessor<Document> implements ILogReceiverProcessable {

    private final ArrayList<IBatchLoggerProcessor> processors;
    private final String registerName;

    public LocalWriterQueue(Properties localWriterConfig, Collection<IBatchLoggerProcessor> processors) {
        this.processors = Lists.newArrayList(
                processors
        );
        registerName = localWriterConfig.getProperty("register.name", "local-queue");
    }

    @Override
    public Collection<IBatchLoggerProcessor> getProcessors() {
        return processors;
    }

    /**
     * 处理批量的日志数据
     *
     * @param logs 日志数据
     * @throws Exception
     */
    @Override
    public void processLoggers(Collection<Document> logs) throws Exception {
        offerData(logs);
    }


    /**
     * 批量数据处理
     *
     * @param batchData 批量数据
     * @throws Exception
     */
    @Override
    protected void processData(List<Document> batchData) throws Exception {
        for (IBatchLoggerProcessor processor : getProcessors()) {
            processor.processLoggers(batchData);
        }
    }

    /**
     * 你丫就睡着吧，另外有一个线程在BaseAsyncProcessor里会处理这些事情
     */
    @Override
    public void run() {
        LocalWriterRegister.getInstance().register(
                registerName,this
        );
        while (true) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                // pass
            }
        }
    }
}
