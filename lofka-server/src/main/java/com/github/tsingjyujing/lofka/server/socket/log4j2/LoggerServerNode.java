package com.github.tsingjyujing.lofka.server.socket.log4j2;

import com.github.tsingjyujing.lofka.basic.LoggerJson;
import com.github.tsingjyujing.lofka.server.queue.IMessageQueue;
import com.github.tsingjyujing.lofka.server.queue.MessageQueueCluster;
import com.github.tsingjyujing.lofka.server.util.SocketServerUtil;
import com.google.common.collect.Lists;
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
@Deprecated
public class LoggerServerNode implements Runnable {
    private final IMessageQueue messageQueueCluster = MessageQueueCluster.getInstance();
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
    }

    @Override
    public void run() {
        try {
            if (objectInputStream != null) {
                Document event;
                while ((event = (Document) objectInputStream.readObject()) != null) {
                    if (!event.containsKey(LoggerJson.TAG_HOST)) {
                        event.append(
                                LoggerJson.TAG_HOST, SocketServerUtil.getHostInfo(socket)
                        );
                        event.append(
                                LoggerJson.TAG_REDIRECT_LINK, Lists.newArrayList(SocketServerUtil.getIPAddress(socket))
                        );
                    }
                    messageQueueCluster.pushQueue(event.toJson());
                }
            }
        } catch (java.io.EOFException e) {
            LOGGER.info("Caught java.io.EOFException closing connection.");
        } catch (java.net.SocketException e) {
            LOGGER.info("Caught java.net.SocketException closing connection.");
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            LOGGER.info("Caught java.io.InterruptedIOException: " + e);
            LOGGER.info("Closing connection.");
        } catch (IOException e) {
            LOGGER.info("Caught java.io.IOException: " + e);
            LOGGER.info("Closing connection.");
        } catch (Exception e) {
            LOGGER.error("Unexpected exception. Closing connection.", e);
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
