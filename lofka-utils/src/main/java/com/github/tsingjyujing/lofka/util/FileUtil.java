package com.github.tsingjyujing.lofka.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileUtil {
    /**
     * 自动读取配置文件
     * 先尝试从文件中获取，再从配置中获取
     *
     * @param propertiesFileName 配置文件名
     * @return
     * @throws IOException
     */
    public static Properties autoReadProperties(String propertiesFileName) throws IOException {
        try {
            // 尝试读取工作目录下conf文件夹
            return readPropertiesFile(propertiesFileName);
        } catch (IOException ex) {
            // 尝试读取资源文件
            return readPropertiesResource(propertiesFileName);
        }
    }


    /**
     * 读取配置（从资源文件根目录中）
     *
     * @param propertiesResourceName 配置文件名
     * @return properties对象
     */
    public static Properties readPropertiesResource(String propertiesResourceName) throws IOException {
        final Properties properties = new Properties();
        final InputStream stream = FileUtil.class.getResourceAsStream("/" + propertiesResourceName);
        properties.load(stream);
        return properties;
    }

    /**
     * 读取配置（默认从工作目录中的conf文件夹）
     *
     * @param propertiesFileName 配置文件名
     * @return properties对象
     */
    public static Properties readPropertiesFile(String propertiesFileName) throws IOException {
        final Properties properties = new Properties();
        final InputStream stream = new FileInputStream("conf/" + propertiesFileName);
        properties.load(stream);
        return properties;
    }


}
