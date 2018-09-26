package com.github.tsingjyujing.lofka.server;

import com.github.tsingjyujing.lofka.server.socket.LoggerSocketServerCluster;
import com.github.tsingjyujing.lofka.server.util.Constants;
import com.github.tsingjyujing.lofka.server.websocket.KafkaReceiver;
import com.github.tsingjyujing.lofka.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

import static com.github.tsingjyujing.lofka.persistence.Entry.runServices;

/**
 * 所有程序启动的入口
 *
 * @author yuanyifan
 */
@SpringBootApplication
public class Entry {
    private final static Logger LOGGER = LoggerFactory.getLogger(Entry.class.getName());

    public static void main(String[] args) {
        // 启动Http服务器
        SpringApplication.run(Entry.class, args);
        // 启动Socket服务器（求求你们别走Socket了）
        LoggerSocketServerCluster.getInstance().startServers();

        try {
            runServices("lofka-persistence.json", true);
        } catch (Exception ex) {
            LOGGER.info("Error while initializing persistence service.", ex);
        }
        try {
            new KafkaReceiver(
                    // 加载配置文件
                    FileUtil.autoReadProperties(Constants.FILE_LOFKA_KAFKA_CLIENT)
            ).run();

        } catch (IOException ex) {
            LOGGER.error("Error while initializing Kafka receiver.", ex);
        }
    }
}
