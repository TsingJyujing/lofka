package com.github.tsingjyujing.lofka.server.socket.logback;

import ch.qos.logback.classic.net.server.HardenedLoggingEventInputStream;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.tsingjyujing.lofka.appender.logback.LoggingEventToJson;
import com.github.tsingjyujing.lofka.basic.IJsonConvert;
import com.github.tsingjyujing.lofka.basic.LoggerJson;
import com.github.tsingjyujing.lofka.server.queue.IMessageQueue;
import com.github.tsingjyujing.lofka.server.queue.MessageQueueCluster;
import com.github.tsingjyujing.lofka.server.util.SocketServerUtil;
import com.google.common.collect.Lists;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.net.Socket;

/**
 * LogBack socket server 节点
 * 每次新的连接创建的时候启动一个
 */
public class LoggerServerNode implements Runnable {
    private final IMessageQueue messageQueueCluster = MessageQueueCluster.getInstance();
    private final Socket socket;
    protected final static Logger LOGGER = LoggerFactory.getLogger(LoggerServer.class);
    private final IJsonConvert<ILoggingEvent> jsonConvertible;
    private boolean isClosed;
    private Document hostInfo;

    public LoggerServerNode(Socket socket) {
        this.socket = socket;
        hostInfo = SocketServerUtil.getHostInfo(socket);
        jsonConvertible = new LoggingEventToJson(new Document(
                LoggerJson.TAG_HOST, hostInfo
        ).append(
                LoggerJson.TAG_REDIRECT_LINK, Lists.newArrayList(SocketServerUtil.getIPAddress(socket))
        ));
    }

    @Override
    public void run() {

        LOGGER.info("Logback socket connection started.");
        try {
            final HardenedLoggingEventInputStream hardenedLoggingEventInputStream = new HardenedLoggingEventInputStream(
                    new BufferedInputStream(
                            socket.getInputStream()
                    )
            );
            // 心静自然凉，Exception自然停
            while (true) {
                final ILoggingEvent event = (ILoggingEvent) hardenedLoggingEventInputStream.readObject();
                if (event != null) {
                    messageQueueCluster.pushQueue(jsonConvertible.toDocument(event).toJson());
                } else {
                    LOGGER.warn("Get a null object from " + hostInfo.toJson());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not open ObjectInputStream to " + socket, e);
        }

    }
}
