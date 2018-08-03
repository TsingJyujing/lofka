package com.github.tsingjyujing.lofka.server.socket.log4j2;

import com.github.tsingjyujing.lofka.server.socket.CommonLoggerServer;

import java.net.Socket;

/**
 * Log4j 2.x socket 服务器
 *
 * 比较特殊，接收的是Document类型的对象
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
        super(port, 1024, "log4j2-doc");
    }

    @Override
    protected Runnable createNodeServer(Socket socket) throws Exception {
        return new LoggerServerNode(socket);
    }

}

