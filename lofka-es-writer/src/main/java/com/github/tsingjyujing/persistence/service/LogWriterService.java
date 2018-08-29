package com.github.tsingjyujing.persistence.service;

import com.github.tsingjyujing.persistence.dao.LogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: zhuyh
 * @date: 2018/8/22
 */

@Component
@Slf4j
public class LogWriterService implements InitializingBean {

    @Autowired
    private LogRepository logRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("writer is started");

    }

}
