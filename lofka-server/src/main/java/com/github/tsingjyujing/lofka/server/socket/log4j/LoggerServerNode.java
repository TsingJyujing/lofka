package com.github.tsingjyujing.lofka.server.socket.log4j;

import com.cvnavi.lofka.appender.log4j.LoggingEventToJson;
import com.cvnavi.lofka.basic.IJsonConvert;
import com.cvnavi.lofka.basic.LoggerJson;
import com.github.tsingjyujing.lofka.server.queue.IMessageQueue;
import com.github.tsingjyujing.lofka.server.queue.MessageQueueCluster;
import com.github.tsingjyujing.lofka.server.util.SocketServerUtil;
import com.google.common.collect.Lists;
import org.apache.log4j.spi.LoggingEvent;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * 有新的Socket连接的时候创建一个线程
 *
 * @author yuanyifan
 */
public class LoggerServerNode implements Runnable {
    /**
     * 消息队列集群，发送到多个消息队列
     */
    private final IMessageQueue messageQueueCluster = MessageQueueCluster.getInstance();

    /**
     * JSON转换工具，将LoggingEvent转换为Document
     */
    private final IJsonConvert<LoggingEvent> jsonConvertible;
    /**
     * 接收的Socket连接
     */
    private final Socket socket;
    private ObjectInputStream objectInputStream;
    protected final static Logger LOGGER = LoggerFactory.getLogger(LoggerServerNode.class);
    private Document hostInfo;

    public LoggerServerNode(Socket socket) throws Exception {
        this.socket = socket;
        objectInputStream = new ObjectInputStream(
                new BufferedInputStream(
                        socket.getInputStream()
                )
        );
        hostInfo = SocketServerUtil.getHostInfo(socket);
        jsonConvertible = new LoggingEventToJson(new Document(
                LoggerJson.TAG_HOST, hostInfo
        ).append(
                LoggerJson.TAG_REDIRECT_LINK, Lists.newArrayList(SocketServerUtil.getIPAddress(socket))
        ));
    }


    @Override
    public void run() {
        try {
            if (objectInputStream != null) {
                while (true) {
                    final LoggingEvent event = (LoggingEvent) objectInputStream.readObject();
                    if (event != null) {
                        messageQueueCluster.pushQueue(jsonConvertible.toDocument(event).toJson());
                    } else {
                        LOGGER.warn("Get a null object from " + hostInfo.toJson());
                    }
                }
            }
        } catch (java.io.EOFException e) {
            LOGGER.info("Caught java.io.EOFException closing conneciton.");
        } catch (java.net.SocketException e) {
            LOGGER.info("Caught java.net.SocketException closing conneciton.");
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            LOGGER.info("Caught java.io.InterruptedIOException: " + e);
            LOGGER.info("Closing connection.");
        } catch (IOException e) {
            LOGGER.info("Caught java.io.IOException: " + e);
            LOGGER.info("Closing connection.");
        } catch (Exception e) {
            LOGGER.error("Unexpected exception. Closing conneciton.", e);
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (Exception e) {
                    LOGGER.info("Could not close connection.", e);
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (InterruptedIOException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException ex) {
                }
            }
        }
    }
}
