package com.github.tsingjyujing.persistence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 日志持久化ES启动类
 */
@SpringBootApplication
@AutoConfigurationPackage
public class LofkaEsWriterApplication {


    public static void main(String[] args) {

        SpringApplication.run(LofkaEsWriterApplication.class, args);
    }


}
