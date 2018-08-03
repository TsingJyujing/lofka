package com.github.tsingjyujing.lofka.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lofka基础配置信息读取
 *
 * @author yuanyifan
 */
public class LofkaConfigUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(LofkaConfigUtil.class);

    private static LofkaConfigUtil ourInstance = new LofkaConfigUtil();

    public static LofkaConfigUtil getInstance() {
        return ourInstance;
    }

    final Properties properties = new Properties();

    private LofkaConfigUtil() {
        try {
            final InputStream stream = new FileInputStream("conf/lofka.properties");
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.error("Error while loading basic config: conf/lofka.properties", e);
        }
    }

    /**
     * 获取应用名称
     *
     * @return
     */
    public String getApplicationName() {
        return properties.getProperty("lofka.application", "");
    }

}
