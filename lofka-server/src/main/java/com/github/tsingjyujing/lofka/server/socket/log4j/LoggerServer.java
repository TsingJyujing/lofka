package com.github.tsingjyujing.lofka.server.socket.log4j;

import com.github.tsingjyujing.lofka.server.socket.CommonLoggerServer;

import java.net.Socket;

/**
 * Log4j 1.x socket 服务器
 * <p>
 * 用于接收原生的SocketAppender的信息
 *
 * @author yuanyifan@cvnavi.com
 */
public class LoggerServer extends CommonLoggerServer {

    /**
     * 日志服务器
     *
     * @param port 接收数据的端口
     */
    public LoggerServer(int port) {
        super(port, 1024, "log4j1");
    }

    @Override
    protected Runnable createNodeServer(Socket socket) throws Exception {
        return new LoggerServerNode(socket);
    }

}
