package com.github.tsingjyujing.lofka.server.util;

import org.bson.Document;

import javax.servlet.http.HttpServletRequest;
import java.net.Socket;
import java.util.Map;

/**
 * Socket Server 工具类
 *
 * @author yuanyifan
 */
public class SocketServerUtil {

    /**
     * 从Socket中解析出格式化的主机信息
     *
     * @param socket Socket 信息
     * @return
     */
    public static Document getHostInfo(Socket socket) {
        Document hostInfo = new Document();
        hostInfo.append("ip", socket.getInetAddress().getHostAddress());
        hostInfo.append("name", socket.getInetAddress().getHostName());
        return hostInfo;
    }

    /**
     * 从Socket连接中获取对方的IP信息
     *
     * @param socket
     * @return
     */
    public static String getIPAddress(Socket socket) {
        return socket.getInetAddress().getHostAddress();
    }

    /**
     * 从HTTP的连接中分析出主机信息
     *
     * @param request HttpServletRequest
     * @return
     */
    public static Map<String, Object> analysisHostInfo(HttpServletRequest request) {
        return new Document(
                "ip", getIPAddress(request)
        ).append(
                "name", request.getRemoteHost()
        );
    }

    /**
     * 从HttpServletRequest中解析出IP地址
     *
     * @param request HttpServletRequest
     * @return
     */
    public static String getIPAddress(HttpServletRequest request) {
        String ip = null;

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
