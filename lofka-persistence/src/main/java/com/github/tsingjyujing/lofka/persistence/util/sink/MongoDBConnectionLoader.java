package com.github.tsingjyujing.lofka.persistence.util.sink;

import com.google.common.collect.Lists;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static com.github.tsingjyujing.lofka.util.FileUtil.autoReadProperties;

/**
 * MongoDB 连接自动加载的基类
 */
public class MongoDBConnectionLoader {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MongoDBConnectionLoader.class);

    private MongoClient mongoDBConnection;

    private boolean isInitialized = false;

    public Properties getProperties() {
        return properties;
    }

    private Properties properties;

    /**
     * 初始化MongoDB连接
     *
     * @param configFileName 配置文件名称（需要在Resource中哟～）
     */
    public MongoDBConnectionLoader(String configFileName) {
        try {
            properties = autoReadProperties(configFileName);
        } catch (IOException e) {
            LOGGER.error("Error while reading properties.", e);
        }
    }

    /**
     * 初始化MongoDB连接
     *
     * @param properties Properties对象（随便你怎么生成的，哈！）
     */
    public MongoDBConnectionLoader(Properties properties) {
        this.properties = properties;
    }


    /**
     * 创建连接对象
     *
     * @return 自动初始化
     */
    public MongoClient getConnection() {
        initialize();
        if (isInitialized) {
            return mongoDBConnection;
        } else {
            throw new RuntimeException("MongoDB connection can't be initialized.");
        }
    }

    /**
     * 通过已经读取完毕的Properties来初始化连接
     */
    private void initialize() {
        if (!isInitialized) {
            try {
                mongoDBConnection = createConnectionFromProperties(properties);
                isInitialized = true;
                LOGGER.info("MongoDBUtil has initialized successfully");
            } catch (Exception ex) {
                LOGGER.error("Error while connecting to mongoDB", ex);
            }
        }
    }

    /**
     * 从配置信息中创建 MongoDB 客户端连接
     *
     * @param properties 配置信息
     * @return
     */
    public static MongoClient createConnectionFromProperties(Properties properties) {
        final MongoClientOptions options = MongoClientOptions.builder()
                .compressorList(Lists.newArrayList(MongoCompressor.createSnappyCompressor()))
                .build();
        final List<ServerAddress> servers = parseServerAddresses(properties.getProperty("mongodb.servers", "localhost:27017"));
        try {
            final MongoCredential credit = parseAuthentication(properties.getProperty("mongodb.auth"));
            if(servers.size()==1){
                return new MongoClient(servers.get(0),credit,options);
            }else {
                return new MongoClient(
                        servers,
                        credit,
                        options
                );
            }
        } catch (Exception ex) {
            if(servers.size()==1){
                return new MongoClient(servers.get(0),options);
            }else {
                return new MongoClient(
                        servers,
                        options
                );
            }
        }
    }

    /**
     * Parse server configuration
     *
     * @param serverConfiguration Format: host1:port1,host2:port2,...,hostn:portn
     * @return server addresses
     */
    public static List<ServerAddress> parseServerAddresses(String serverConfiguration) {
        List<ServerAddress> hosts = Lists.newArrayList();
        for (String subConfig : serverConfiguration.split(",")) {
            String[] hostInfo = subConfig.split(":");
            if (hostInfo.length == 1) {
                hosts.add(new ServerAddress(hostInfo[0], 27017));
            } else if (hostInfo.length == 2) {
                hosts.add(new ServerAddress(hostInfo[0], Integer.parseInt(hostInfo[1])));
            } else {
                throw new RuntimeException("Invalid server address serverConfiguration:" + subConfig);
            }
        }
        return hosts;
    }

    /**
     * Parse authentic configuration
     *
     * @param authenticConfig Format: user:password@dbname
     * @return MongoCredential
     */
    public static MongoCredential parseAuthentication(String authenticConfig) {
        final String[] userInfo = authenticConfig.split(":");
        if (userInfo.length == 2) {
            final String userName = userInfo[0];
            final String[] dbInfo = userInfo[1].split("@");
            if (dbInfo.length == 2) {
                final String dbName = dbInfo[1];
                final char[] password = dbInfo[0].toCharArray();
                return MongoCredential.createCredential(userName, dbName, password);
            }
        }
        throw new RuntimeException("Invalid authentic config.");
    }
}
