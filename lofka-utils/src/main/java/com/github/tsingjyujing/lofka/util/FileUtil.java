package com.github.tsingjyujing.lofka.util;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
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
        return autoReadProperties(propertiesFileName, FileUtil.class);
    }

    /**
     * 自动读取配置文件
     * 先尝试从文件中获取，再从配置中获取
     *
     * @param propertiesFileName 配置文件名
     * @return
     * @throws IOException
     */
    public static Properties autoReadProperties(String propertiesFileName, Class c) throws IOException {
        try {
            // 尝试读取工作目录下conf文件夹
            return readPropertiesFile(propertiesFileName);
        } catch (IOException ex) {
            // 尝试读取资源文件
            return readPropertiesResource(propertiesFileName, c);
        }
    }


    /**
     * 读取配置（从资源文件根目录中）
     *
     * @param propertiesResourceName 配置文件名
     * @return properties对象
     */
    public static Properties readPropertiesResource(String propertiesResourceName) throws IOException {
        return readPropertiesResource(propertiesResourceName, FileUtil.class);
    }


    /**
     * 读取配置（从资源文件根目录中）
     *
     * @param propertiesResourceName 配置文件名
     * @return properties对象
     */
    public static Properties readPropertiesResource(String propertiesResourceName, Class c) throws IOException {
        final Properties properties = new Properties();
        final InputStream stream = c.getResourceAsStream("/" + propertiesResourceName);
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

    /**
     * 获取给定文件名的文本内容
     *
     * @param filename 文件名称
     * @return
     */
    public static String getFileText(String filename) throws IOException {
        try {
            return convert(new FileInputStream("conf/" + filename));
        } catch (Exception ex) {
            return convert(FileUtil.class.getResourceAsStream("/" + filename));
        }
    }

    /**
     * 将流转换为文本
     *
     * @param inputStream
     * @param charset
     * @return
     * @throws IOException
     */
    public static String convert(InputStream inputStream, Charset charset) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        return stringBuilder.toString();
    }


    /**
     * 将流转换为文本
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String convert(InputStream inputStream) throws IOException {
        return convert(inputStream, Charset.forName("UTF-8"));
    }

    /**
     * 读取配置（从资源文件根目录中）
     *
     * @param textResourceName 配置文件名
     * @return properties对象
     */
    public static String readTextResource(String textResourceName) throws IOException {
        return IOUtils.toString(
                FileUtil.class.getResourceAsStream("/" + textResourceName), "UTF-8"
        );
    }

    /**
     * 读取配置（默认从工作目录中的conf文件夹）
     *
     * @param textFileName 配置文件名
     * @return properties对象
     */
    public static String readTextFile(String textFileName) throws IOException {
        return IOUtils.toString(
                new FileInputStream("conf/" + textFileName), "UTF-8"
        );
    }

}

