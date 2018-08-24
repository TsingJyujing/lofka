package com.github.tsingjyujing.lofka.server.websocket;

import com.github.tsingjyujing.lofka.server.util.Constants;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * WebSocket日志推流
 *
 * @author yuanyifan
 */
@ServerEndpoint(value = "/" + Constants.INTERFACE_WEBSOCKET_PUSH)
@Component
public class LoggerPushWebSocket {


    private final static Logger LOGGER = LoggerFactory.getLogger(LoggerPushWebSocket.class.getName());

    /**
     * 在线对象
     */
    private final static AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    /**
     * 存放每个客户端对应的MyWebSocket对象
     */
    private final static CopyOnWriteArraySet<LoggerPushWebSocket> WEB_SOCKET_CLIENTS = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    private final FilterEngine filterEngine = new FilterEngine();
    private Gson gson = new Gson();

    /**
     * 连接建立成功调用的方法
     *
     * @param session 当前会话session
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        WEB_SOCKET_CLIENTS.add(this);
        ONLINE_COUNT.set(WEB_SOCKET_CLIENTS.size());
        LOGGER.info("New web socket established: {}, current clients: {}", session.getId(), ONLINE_COUNT.get());
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        WEB_SOCKET_CLIENTS.remove(this);
        ONLINE_COUNT.set(WEB_SOCKET_CLIENTS.size());
        LOGGER.info("An connection closed: {}, current clients: {}", session.getId(), ONLINE_COUNT.get());
    }

    /**
     * 收到客户端消息
     *
     * @param message 客户端发送过来的消息
     * @param session 当前会话session
     * @throws IOException
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        try {
            filterEngine.setFilter(message);
            final Map<String,Object> messageOk = Maps.newHashMap();
            messageOk.put("status",200);
            messageOk.put("echo",message);
            forceSendMessage(gson.toJson(messageOk));
            LOGGER.info("Get an message from client {}: \n{}", session.getId(), message);
        } catch (Exception ex) {
            final Map<String,Object> messageFail = Maps.newHashMap();
            messageFail.put("status",500);
            messageFail.put("echo",message);
            forceSendMessage(gson.toJson(messageFail));
            LOGGER.warn("Error while setting filter:", ex);
        }
    }

    /**
     * 发生错误的时候记录
     *
     * @param session 会话
     * @param error   错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.error("Error in connection :" + session.getId(), error);
    }

    /**
     * 群发消息
     *
     * @param message 消息内容
     * @throws IOException
     */
    public static void sendMessageGrouply(String message) {
        if (getOnlineCount() > 0) {
            for (LoggerPushWebSocket item : WEB_SOCKET_CLIENTS) {
                try {
                    item.sendMessage(message);
                } catch (Exception ex) {
                    //pass
                    LOGGER.info("Error while transmitting message to client:" + item.session.getId(), ex);
                }
            }
            LOGGER.trace("Send an message to client grouply:", ONLINE_COUNT.get());
        } else {
            LOGGER.trace("No connection established, message ignored.");
        }
    }

    /**
     * 给当前连接的客户端发送一条消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        if (filterEngine.getFilterResult(message)) {
            forceSendMessage(message);
        }
    }

    /**
     * 强制发送消息
     * @param message
     * @throws IOException
     */
    public void forceSendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
        LOGGER.trace("Send an message to client:", session.getId());
    }

    /**
     * 获取在线数量
     *
     * @return
     */
    public static int getOnlineCount() {
        return LoggerPushWebSocket.ONLINE_COUNT.get();
    }

    @Override
    public int hashCode() {
        return session.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoggerPushWebSocket) {
            return ((LoggerPushWebSocket) obj).session.getId().equals(session.getId());
        } else {
            return false;
        }
    }
}
