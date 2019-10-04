package com.github.tsingjyujing.lofka.persistence.writers;

import com.github.tsingjyujing.lofka.persistence.basic.IBatchLoggerProcessor;
import com.github.tsingjyujing.lofka.persistence.util.DocumentUtil;
import com.github.tsingjyujing.lofka.util.FileUtil;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.bson.Document;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 * 将日志写入 ElasticSearch
 */
public class ElasticSearchWriter implements IBatchLoggerProcessor {
    protected final static Logger LOGGER = LoggerFactory.getLogger(ElasticSearchWriter.class);
    private final long expiredMills;
    private final Document expiredSetting;
    private final String esIndexName;
    private final RestHighLevelClient client;

    /**
     * 初始化MongoDB连接
     *
     * @param config 配置信息
     */
    public ElasticSearchWriter(Properties config) throws IOException {
        esIndexName = config.getProperty("es.naming.index", "lofka");
        expiredMills = Long.parseLong(config.getProperty("es.deprecate.time", "3600000"));
        expiredSetting = DocumentUtil.getExpiredSetting(config, "es.expired.setting");
        final String[] hostInfoRaw = config.getProperty("es.servers").split(",");
        final HttpHost[] hosts = new HttpHost[hostInfoRaw.length];
        for (int i = 0; i < hostInfoRaw.length; i++) {
            hosts[i] = HttpHost.create(hostInfoRaw[i]);
        }
        final RestClientBuilder clientBuilder = RestClient.builder(
                hosts
        );

        if (config.containsKey("es.auth.user") && config.containsKey("es.auth.password")) {
            final String user = config.getProperty("es.auth.user");
            final String password = config.getProperty("es.auth.password");
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(
                        HttpAsyncClientBuilder httpClientBuilder) {
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        } else {
            LOGGER.warn("ElasticSearch authentication haven't set, maybe unsafe.");
        }

        client = new RestHighLevelClient(clientBuilder);
        final boolean indexExist = client.indices().exists(new GetIndexRequest().indices(esIndexName));
        if (!indexExist) {
            client.indices().create(
                    new CreateIndexRequest(esIndexName)
            );
        }
    }


    /**
     * 初始化MongoDB连接
     *
     * @param configFileName 配置文件名称（需要在Resource中哟～）
     */
    public ElasticSearchWriter(String configFileName) throws IOException {
        this(FileUtil.autoReadProperties(configFileName));
    }

    /**
     * 加载默认配置文件
     *
     * @throws IOException
     */
    public ElasticSearchWriter() throws IOException {
        this("persistence/elasticsearch.properties");
    }

    /**
     * Process (print or persistence it)
     *
     * @param logs log in LoggerJson format
     */
    @Override
    public void processLoggers(Collection<Document> logs) {
        final BulkRequest bulkRequest = new BulkRequest();
        final long timeoutLimit = System.currentTimeMillis() + expiredMills;
        for (Document log : logs) {
            final Document processedLog = DocumentUtil.mongoLogProcessor(
                    log,
                    this.expiredSetting
            );
            if (processedLog.getDate("expired_time").getTime() >= timeoutLimit) {
                bulkRequest.add(
                        new IndexRequest(
                                esIndexName
                        ).source(
                                log.toJson(),
                                XContentType.JSON
                        ).type(
                                "doc"
                        )
                );
            }
        }

        this.client.bulkAsync(
                bulkRequest, new ActionListener<BulkResponse>() {
                    @Override
                    public void onResponse(BulkResponse bulkItemResponses) {
                        // Doing nothing
                    }

                    @Override
                    public void onFailure(Exception e) {
                        LOGGER.error("Error while commit data to ElasticSearch.", e);
                    }
                }
        );
    }


}
