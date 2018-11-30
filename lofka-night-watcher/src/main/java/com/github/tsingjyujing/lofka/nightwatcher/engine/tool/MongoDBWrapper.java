package com.github.tsingjyujing.lofka.nightwatcher.engine.tool;

import com.google.common.collect.Lists;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * MongoDB的封装
 */
public class MongoDBWrapper implements AutoCloseable {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MongoDBWrapper.class);

    private MongoClient mongoDBConnection;

    private boolean isInitialized = false;

    /**
     * Make POJO Happy
     *
     * @return
     */
    public Properties getProperties() {
        return properties;
    }

    private Properties properties;

    /**
     * 初始化MongoDB连接
     *
     * @param properties Properties对象（随便你怎么生成的，哈！）
     */
    public MongoDBWrapper(Properties properties) {
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
     * 获取数据库
     *
     * @param dbName 数据库名称
     * @return
     */
    public MongoDatabase getDatabase(String dbName) {
        return getConnection().getDatabase(dbName);
    }

    /**
     * 获取 Collection
     *
     * @param dbName         数据库名称
     * @param collectionName Collection名称
     * @return
     */
    public MongoCollection<Document> getCollection(String dbName, String collectionName) {
        return getDatabase(dbName).getCollection(collectionName);
    }

    /**
     * 通过JSON格式的配置信息创建连接代理
     *
     * @param jsonSettings JSON配置，和Properties差不多
     * @return
     */
    public static MongoDBWrapper createFromJsonString(String jsonSettings) {
        final Properties prop = new Properties();
        prop.putAll(Document.parse(jsonSettings));
        return new MongoDBWrapper(prop);
    }


    /**
     * 通过已经读取完毕的Properties来初始化连接
     */
    private void initialize() {
        if (!isInitialized) {
            try {
                final MongoClientOptions options = MongoClientOptions.builder()
                        .compressorList(Lists.newArrayList(MongoCompressor.createSnappyCompressor()))
                        .build();

                try {
                    mongoDBConnection = new MongoClient(
                            parseServerAddresses(properties.getProperty("servers", "localhost:27017")),
                            parseAuthentication(properties.getProperty("auth")),
                            options
                    );
                } catch (Exception ex) {
                    mongoDBConnection = new MongoClient(
                            parseServerAddresses(properties.getProperty("servers", "localhost:27017")),
                            options
                    );
                }
                isInitialized = true;
                LOGGER.info("MongoDBUtil has initialized successfully");
            } catch (Exception ex) {
                LOGGER.error("Error while connecting to mongoDB", ex);
            }
        }
    }


    /**
     * Parse server configuration
     *
     * @param serverConfiguration Format: host1:port1,host2:port2,...,hostn:portn
     * @return server addresses
     */
    private static List<ServerAddress> parseServerAddresses(String serverConfiguration) {
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
    private static MongoCredential parseAuthentication(String authenticConfig) {
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

    @Override
    public void close() throws Exception {
        if (isInitialized) {
            mongoDBConnection.close();
        }
    }

    public static void upsertOne(MongoCollection<Document> coll, Bson filter, Bson data) {
        coll.updateOne(filter, new Document("$set", data), new UpdateOptions().upsert(true));
    }
}
