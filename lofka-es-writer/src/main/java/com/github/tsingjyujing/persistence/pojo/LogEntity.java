package com.github.tsingjyujing.persistence.pojo;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 日志实体类
 *
 * @author: zhuyh
 * @date: 2018/8/22
 */
@Data
@Document(indexName = "logger-json",type = "logger-json", shards = 1,replicas = 0, refreshInterval = "-1")
public class LogEntity {

    @Transient
    private String type;

    private String id;
    private Boolean needFile;
    private String traceId;

    private Long timestamp;
    private Long write_timestamp;
    private Long expired_time;
    private String app_name;
    private String host;
    private String level;
    private String kafka_info;
    private String logger;
    private String message;


}

@Data
class Message {
    private String uri;
    private String host;
    private String request_time;
    private String remote_addr;
    private String upstream_response_time;
    private String status;
    private String http_user_agent;

}

