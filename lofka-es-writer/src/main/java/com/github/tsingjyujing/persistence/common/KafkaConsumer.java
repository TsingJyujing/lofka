package com.github.tsingjyujing.persistence.common;

import com.alibaba.fastjson.JSONObject;
import com.github.tsingjyujing.persistence.dao.LogRepository;
import com.github.tsingjyujing.persistence.pojo.LogEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: zhuyh
 * @date: 2018/8/22
 */
@Component
@Slf4j
public class KafkaConsumer {

    @Autowired
    private LogRepository logRepository;

    /**
     * 监听日志消息，这里的groupId在KafkaConsumerConfig已经设置可以覆盖
     * @param content
     */
    @KafkaListener(topics = {"logger-json"},groupId = "test")
    public void processMessage(String content) {

        log.debug("kafka consumer message:{}", content);

        LogEntity logEntity = JSONObject.parseObject(content,LogEntity.class);

        logRepository.save(logEntity);


    }

}
