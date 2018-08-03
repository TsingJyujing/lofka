package com.github.tsingjyujing.lofka.server.socket.logback;

import com.github.tsingjyujing.lofka.server.socket.CommonLoggerServer;

import java.net.Socket;

/**
 * logback socket 服务器
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
        super(port, 1024, "logback");
    }

    @Override
    protected Runnable createNodeServer(Socket socket) throws Exception {
        return new LoggerServerNode(socket);
    }

}
