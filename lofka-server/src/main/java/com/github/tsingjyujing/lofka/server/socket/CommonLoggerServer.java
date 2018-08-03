package com.github.tsingjyujing.lofka.server.socket;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 通用Socket连接池服务器
 *
 * @author yuanyifan@cvnavi.com
 */
public abstract class CommonLoggerServer implements LoggerSocketServer {
    private final int port;
    private final int threadPoolSize;
    private final String serverName;
    private boolean isClosed = false;
    private final ThreadFactory namedThreadFactory;
    private final ThreadPoolExecutor threadPool;

    protected final static Logger LOGGER = LoggerFactory.getLogger(CommonLoggerServer.class);

    /**
     * 日志服务器
     *
     * @param port 接收数据的端口
     */
    public CommonLoggerServer(int port, int threadPoolSize, String serverName) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.serverName = serverName;
        this.namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(serverName + "-node-%d").build();
        this.threadPool = new ThreadPoolExecutor(
                4, threadPoolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(threadPoolSize + 10),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );

    }

    /**
     * 根据Socket信息创建处理线程
     *
     * @param socket
     * @return
     * @throws Exception
     */
    abstract protected Runnable createNodeServer(Socket socket) throws Exception;


    /**
     * 启动线程接收数据
     */
    @Override
    public void run() {
        LOGGER.info(String.format("Starting %s socket server, listening on :%d", serverName, port));
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            // 设置优雅关闭
            Runtime.getRuntime().addShutdownHook(
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        LOGGER.info(String.format("Closing %s socket server, listening on :%d.", serverName, port));
                                        isClosed = true;
                                        // 关闭连接池
                                        serverSocket.close();
                                        // 等待所有线程崩溃
                                        threadPool.shutdown();
                                        LOGGER.info(String.format("%s socket server, listening on :%d has closed normally.", serverName, port));
                                    } catch (IOException e) {
                                        LOGGER.error(
                                                String.format(
                                                        "Error while closing %s socket server which listening on :%d",
                                                        serverName,
                                                        port
                                                ),
                                                e
                                        );
                                    }
                                }
                            }
                    )
            );
            while (!isClosed()) {
                try {
                    LOGGER.info("Waiting to accept a new client.");
                    // 接收到连接请求的时候，创建一个线程处理该连接的事务
                    Socket socket = serverSocket.accept();
                    LOGGER.info("Connected to client at " + socket.getInetAddress());
                    LOGGER.info("Starting new socket node.");
                    threadPool.execute(createNodeServer(socket));
                } catch (SocketException socketEx) {
                    if ("Socket closed".equals(socketEx.getMessage())) {
                        LOGGER.info(String.format("%s socket server, listening on :%d, waiting operation aborted by socket close.", serverName, port));
                    } else {
                        LOGGER.warn(String.format("Error while accepting connection of %s socket server (SocketException)", serverName), socketEx);
                    }
                } catch (Exception ex) {
                    LOGGER.error(String.format("Error while accepting connection of %s socket server", serverName), ex);
                }
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("Error while starting %s socket server", serverName), ex);
        }
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * 用来监控SocketServer的状态
     *
     * @return
     */
    @Override
    public Document getMonitorInfo() {
        return new Document(
                "name", serverName
        ).append(
                "port", port
        ).append(
                "max_concurrency", threadPoolSize
        ).append(
                "active_count", threadPool.getActiveCount()
        ).append(
                "largest_pool_size", threadPool.getLargestPoolSize()
        ).append(
                "max_pool_size", threadPool.getMaximumPoolSize()
        ).append(
                "core_pool_size", threadPool.getCorePoolSize()
        ).append(
                "pool_size", threadPool.getPoolSize()
        ).append(
                "task_count", threadPool.getTaskCount()
        );
    }
}