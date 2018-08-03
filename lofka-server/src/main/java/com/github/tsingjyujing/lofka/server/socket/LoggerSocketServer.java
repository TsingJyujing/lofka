package com.github.tsingjyujing.lofka.server.socket;

import org.bson.Document;

/**
 * 所有socket server都需要继承这个类
 *
 * @author yuanyifan
 */
public interface LoggerSocketServer extends Runnable {
    /**
     * 获取端口信息
     *
     * @return
     */
    int getPort();

    /**
     * 是否已经关闭
     *
     * @return
     */
    boolean isClosed();

    /**
     * 用来监控SocketServer的状态
     *
     * @return
     */
    Document getMonitorInfo();
}
