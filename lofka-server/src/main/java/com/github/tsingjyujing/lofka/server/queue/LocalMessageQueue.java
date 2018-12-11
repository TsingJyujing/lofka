package com.github.tsingjyujing.lofka.server.queue;

import com.github.tsingjyujing.lofka.basic.BaseAsyncProcessor;
import com.github.tsingjyujing.lofka.persistence.source.LocalWriterRegister;
import org.bson.Document;

import java.util.List;

/**
 * 本地消息队列写入
 */
public class LocalMessageQueue extends BaseAsyncProcessor<Document> implements IMessageQueue {

    public LocalMessageQueue(){

    }

    /**
     * 批量数据处理
     *
     * @param batchData 批量数据
     * @throws Exception
     */
    @Override
    protected void processData(List<Document> batchData) throws Exception {
        LocalWriterRegister.getInstance().processLoggers(
                batchData
        );
    }

    /**
     * 推送消息
     *
     * @param message 消息正文
     * @throws Exception 推送异常
     */
    @Override
    public void pushQueue(String message) throws Exception {
        offerData(Document.parse(message));
    }
}
