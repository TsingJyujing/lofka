package com.github.tsingjyujing.lofka.persistence.writers;

import com.github.tsingjyujing.lofka.persistence.basic.IBatchLoggerProcessor;
import com.github.tsingjyujing.lofka.persistence.util.DocumentUtil;
import com.github.tsingjyujing.lofka.persistence.util.sink.MongoDBConnectionLoader;
import com.github.tsingjyujing.lofka.util.FileUtil;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 将日志写入 MongoDB
 */
public class MongoDBWriter extends MongoDBConnectionLoader implements IBatchLoggerProcessor {
    private static final int ASCENDING = 1;
    private static final String HASHED = "hashed";

    private final MongoCollection<Document> loggerCollection;
    private final long expiredMills;
    private final Document expiredSetting;

    /**
     * 初始化MongoDB连接
     *
     * @param config 配置信息
     */
    public MongoDBWriter(Properties config) {
        super(config);
        loggerCollection = getConnection().getDatabase(
                getProperties().getProperty("mongodb.database", "logger")
        ).getCollection(
                getProperties().getProperty("mongodb.collection", "lofka")
        );
        expiredMills = Long.parseLong(getProperties().getProperty("mongodb.deprecate.time", "3600000"));
        expiredSetting = DocumentUtil.getExpiredSetting(getProperties(), "mongodb.expired.setting");
        createIndexes(loggerCollection);
    }

    /**
     * 初始化MongoDB连接
     *
     * @param configFileName 配置文件名称（需要在Resource中哟～）
     */
    public MongoDBWriter(String configFileName) throws IOException {
        this(FileUtil.autoReadProperties(configFileName));
    }

    /**
     * 加载默认配置文件
     *
     * @throws IOException
     */
    public MongoDBWriter() throws IOException {
        this("persistence/mongodb.properties");
    }

    private final InsertManyOptions insertOption = new InsertManyOptions().ordered(false);

    /**
     * Process (print or persistence it)
     *
     * @param logs log in LoggerJson format
     */
    @Override
    public void processLoggers(Collection<Document> logs) {
        final long deprecatedTick = System.currentTimeMillis() + expiredMills;
        final List<Document> documentList = Lists.newArrayList();
        try {
            for (Document log : logs) {
                Document processedLog = DocumentUtil.mongoLogProcessor(
                        log, expiredSetting
                );
                if (processedLog.getDate("expired_time").getTime() >= deprecatedTick) {
                    documentList.add(
                            processedLog
                    );
                }
            }
            if (!documentList.isEmpty()) {
                loggerCollection.insertMany(documentList, insertOption);
            }
        } catch (Exception ex) {
            LOGGER.debug(
                    String.format(
                            "Error while insert %d batch logs into mongodb: ",
                            documentList.size()
                    ), ex);
            for (Document document : documentList) {
                try {
                    loggerCollection.insertOne(document);
                } catch (Exception exOne) {
                    LOGGER.debug("Error while insert one log into mongodb: ", exOne);
                }
            }
        }
    }

    /**
     * 为存储的Collection创建索引
     *
     * @param collection 存储
     */
    private static void createIndexes(MongoCollection<Document> collection) {
        collection.createIndexes(Lists.newArrayList(
                new IndexModel(new Document("timestamp", ASCENDING)),
                new IndexModel(new Document("write_timestamp", ASCENDING)),
                new IndexModel(new Document("expired_time", ASCENDING), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS)),
                new IndexModel(new Document("app_name", HASHED), new IndexOptions().sparse(true)),
                new IndexModel(new Document("host.ip", HASHED)),
                new IndexModel(new Document("level", HASHED)),
                // 分片儿模式不支持
                // new IndexModel(new Document("kafka_info", ASCENDING), new IndexOptions().unique(true)),
                new IndexModel(new Document("logger", HASHED))
        ));
        final IndexOptions nginxPartialIndexOption = new IndexOptions().partialFilterExpression(
                new Document("type", "NGINX")
        );
        collection.createIndexes(Lists.newArrayList(
                new IndexModel(
                        new Document(
                                "message.uri", ASCENDING
                        ).append(
                                "message.host", ASCENDING
                        ),
                        nginxPartialIndexOption
                ),
                new IndexModel(
                        new Document(
                                "message.request_time", ASCENDING
                        ),
                        nginxPartialIndexOption
                ),
                new IndexModel(
                        new Document(
                                "message.remote_addr", HASHED
                        ),
                        nginxPartialIndexOption
                ),
                new IndexModel(
                        new Document(
                                "message.upstream_response_time", ASCENDING
                        ),
                        nginxPartialIndexOption
                ),
                new IndexModel(
                        new Document(
                                "message.status", HASHED
                        ),
                        nginxPartialIndexOption
                ),
                new IndexModel(
                        new Document(
                                "message.http_user_agent", HASHED
                        ),
                        nginxPartialIndexOption
                )
        ));
    }
}
