package com.github.tsingjyujing.lofka.nightwatcher.engine.tool;

import com.github.tsingjyujing.lofka.persistence.util.sink.MongoDBConnectionLoader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                mongoDBConnection = MongoDBConnectionLoader.createConnectionFromProperties(properties);
                isInitialized = true;
                LOGGER.info("MongoDBUtil has initialized successfully");
            } catch (Exception ex) {
                LOGGER.error("Error while connecting to mongoDB", ex);
            }
        }
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
