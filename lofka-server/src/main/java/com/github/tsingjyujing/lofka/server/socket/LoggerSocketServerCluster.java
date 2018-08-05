package com.github.tsingjyujing.lofka.server.socket;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 日志服务器集群，负责启动所有的Socket日志服务器
 *
 * @author yuanyifan
 */
public class LoggerSocketServerCluster {
    private static LoggerSocketServerCluster ourInstance = new LoggerSocketServerCluster();

    public static LoggerSocketServerCluster getInstance() {
        return ourInstance;
    }

    private boolean isServerStarted = false;

    /**
     * 获取Socket服务器组，但是不允许更改，只允许访问
     * @return
     */
    public List<? extends LoggerSocketServer> getSocketServers() {
        return Lists.newArrayList(socketServers);
    }

    /**
     * 所有需要启动的Socket服务器都添加到这里
     * 目前不再启动Log4j2的Socket服务器
     */
    private final List<? extends LoggerSocketServer> socketServers = Lists.newArrayList(
            new com.github.tsingjyujing.lofka.server.socket.log4j.LoggerServer(9501),
            new com.github.tsingjyujing.lofka.server.socket.logback.LoggerServer(9503)
    );

    private final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("logger-server-cluster-%d").build();

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            socketServers.size(), socketServers.size(),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(socketServers.size() + 4),
            namedThreadFactory,
            new ThreadPoolExecutor.AbortPolicy()
    );

    private LoggerSocketServerCluster() {
        startServers();
    }

    /**
     * 启动服务器，同时避免重复启动
     */
    public void startServers() {
        if (!isServerStarted) {
            for (LoggerSocketServer server : socketServers) {
                // 依次启动所有Socket服务器
                threadPool.execute(server);
            }
            isServerStarted = true;
        }
    }

    /**
     * 获取所有服务器的状态
     *
     * @return
     */
    public Document getAllStatus() {
        List<Document> serverStatus = Lists.newArrayList();
        for (LoggerSocketServer server : socketServers) {
            serverStatus.add(server.getMonitorInfo());
        }
        return new Document(
                "server_count", serverStatus.size()
        ).append(
                "active_server_count", threadPool.getActiveCount()
        ).append(
                "server_details", serverStatus
        );
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        threadPool.shutdown();
    }
}
