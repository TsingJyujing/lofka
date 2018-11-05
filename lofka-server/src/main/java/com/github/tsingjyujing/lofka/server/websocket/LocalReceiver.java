package com.github.tsingjyujing.lofka.server.websocket;

import com.github.tsingjyujing.lofka.persistence.basic.ILogReceiver;
import com.github.tsingjyujing.lofka.persistence.source.LocalWriterRegister;
import org.bson.Document;

import java.util.Collection;

/**
 * 接受本地日志并且推送到
 */
public class LocalReceiver implements ILogReceiver {

    /**
     * 处理批量的日志数据
     *
     * @param logs 日志数据
     * @throws Exception
     */
    @Override
    public void processLoggers(Collection<Document> logs) throws Exception {
        Exception lastEx = null;
        for (Document log : logs) {
            try {
                LoggerPushWebSocket.sendMessageGrouply(log.toJson());
            } catch (Exception ex) {
                lastEx = ex;
            }
        }
        if (lastEx != null) {
            throw lastEx;
        }
    }


    @Override
    public void run() {
        LocalWriterRegister.getInstance().register(
                "websocket-push", this
        );
        while (true) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                //
            }
        }
    }
}
