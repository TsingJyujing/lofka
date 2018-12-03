package com.github.tsingjyujing.lofka.server.util;

/**
 * 一些常见的常量疯封装
 */
public class Constants {
    // 推送接口信息
    public static final String INTERFACE_PUSH_SINGLE = com.github.tsingjyujing.lofka.util.Constants.INTERFACE_PUSH_SINGLE;
    public static final String INTERFACE_PUSH_BATCH = com.github.tsingjyujing.lofka.util.Constants.INTERFACE_PUSH_BATCH;
    public static final String INTERFACE_PUSH_SINGLE_ZIP = com.github.tsingjyujing.lofka.util.Constants.INTERFACE_PUSH_SINGLE_ZIP;
    public static final String INTERFACE_PUSH_BATCH_ZIP = com.github.tsingjyujing.lofka.util.Constants.INTERFACE_PUSH_BATCH_ZIP;
    // 其他接口信息
    public static final String INTERFACE_PUSH_UNSAFE = "lofka/service/push/unsafe";
    public static final String INTERFACE_KAFKA_PUSH = "lofka/kafka/api/push";
    public static final String INTERFACE_SERVER_MONITOR = "lofka/socket/servers/monitor";
    public static final String INTERFACE_WEBSOCKET_PUSH = "lofka/websocket/logs";
    //配置文件信息
    public static final String FILE_LOFKA_QUEUE_KAFKA = "lofka-kafka.properties";
    public static final String FILE_LOFKA_QUEUE_REDIR = "lofka-redirect.properties";
    public static final String FILE_LOFKA_KAFKA_CLIENT = "lofka-kafka-client.properties";
}
