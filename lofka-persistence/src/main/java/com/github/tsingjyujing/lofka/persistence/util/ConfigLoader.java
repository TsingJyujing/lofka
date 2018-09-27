package com.github.tsingjyujing.lofka.persistence.util;

import com.github.tsingjyujing.lofka.persistence.basic.IBatchLoggerProcessor;
import com.github.tsingjyujing.lofka.persistence.basic.ILogReceiver;
import com.github.tsingjyujing.lofka.persistence.source.KafkaMultiPersistence;
import com.github.tsingjyujing.lofka.persistence.writers.LocalFileWriter;
import com.github.tsingjyujing.lofka.persistence.writers.MongoDBWriter;
import com.github.tsingjyujing.lofka.util.FileUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 配置文件加载器
 *
 * @author yuanyifan
 */
public class ConfigLoader {

    private static final Gson GSON = new Gson();
    protected static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    /**
     * 消息处理器的工厂方法
     *
     * @param processorInfo 需要初始化的处理器信息
     * @return
     * @throws ClassNotFoundException 无法找到该初始化方法
     * @throws IOException            无法读取相应的配置文件
     */
    public static IBatchLoggerProcessor processorFactory(ProcessorInfo processorInfo) throws Exception {
        switch (processorInfo.getProcessorType().toLowerCase()) {
            case "mongodb":
                return new MongoDBWriter(processorInfo.getProperties());
            case "file":
                return new LocalFileWriter(processorInfo.getProperties());
            default:
                throw new ClassNotFoundException("Can't initialize by name:" + processorInfo.getProcessorType());
        }
    }

    /**
     * 数据源的工厂方法
     *
     * @param srcInfo
     * @param processors
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ILogReceiver sourceFactory(SourceInfo srcInfo, Iterable<IBatchLoggerProcessor> processors) throws Exception {
        switch (srcInfo.getSourceType()) {
            case "kafka":
                return new KafkaMultiPersistence(
                        srcInfo.getProperties(),
                        processors
                );
            default:
                throw new ClassNotFoundException("Can't initialize by name:" + srcInfo.getSourceType());
        }
    }

    /**
     * 方便解析配置文件的实体类
     */
    private class ProcessorInfo {
        public String getProcessorType() {
            return processorType;
        }

        public void setProcessorType(String processorType) {
            this.processorType = processorType;
        }

        private String processorType;

        public Map<String, String> getConfig() {
            return config;
        }

        public void setConfig(Map<String, String> config) {
            this.config = config;
        }

        private Map<String, String> config;

        public IBatchLoggerProcessor getProcessor() throws Exception {
            return processorFactory(this);
        }

        public Properties getProperties() {
            final Properties prop = new Properties();
            prop.putAll(getConfig());
            return prop;
        }
    }


    /**
     * 解析Source配置文件
     */
    private class SourceInfo {

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        private String sourceType;

        public Map<String, String> getConfig() {
            return config;
        }

        public void setConfig(Map<String, String> config) {
            this.config = config;
        }

        private Map<String, String> config;


        public List<ProcessorInfo> getProcessors() {
            final ArrayList<ProcessorInfo> processorList = Lists.newArrayList();
            for (Object processor : processors.values()) {
                processorList.add(GSON.fromJson(GSON.toJson(processor), ProcessorInfo.class));
            }
            return processorList;
        }

        public void setProcessors(Map<String, Object> processors) {
            this.processors = processors;
        }

        private Map<String, Object> processors;

        public ILogReceiver getSource() throws Exception {
            ArrayList<IBatchLoggerProcessor> result = Lists.newArrayList();
            for (ProcessorInfo processorInfo : getProcessors()) {
                result.add(processorInfo.getProcessor());
            }
            return sourceFactory(this, result);
        }

        public Properties getProperties() {
            final Properties prop = new Properties();
            prop.putAll(getConfig());
            return prop;
        }
    }

    /**
     * 从配置文件中加载数据源
     *
     * @param jsonConfigFile JSON配置文件
     * @return
     * @throws IOException
     */
    public static ArrayList<ILogReceiver> loadSource(String jsonConfigFile) throws Exception {
        final ArrayList<ILogReceiver> result = Lists.newArrayList();
        for (SourceInfo sourceInfo : stringToList(FileUtil.getFileText(jsonConfigFile), SourceInfo.class)) {
            result.add(sourceInfo.getSource());
        }
        return result;
    }

    /**
     * 从默认配置文件中加载处理器
     *
     * @return
     * @throws IOException
     */
    public static ArrayList<ILogReceiver> loadSource() throws Exception {
        return loadSource("source.json");
    }


    /**
     * JSON String 转成 bean list
     *
     * @param gsonString
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> stringToList(String gsonString, Class<T> cls) {
        final ArrayList<T> result = new ArrayList<>();
        for (final JsonElement elem : new JsonParser().parse(gsonString).getAsJsonArray()) {
            Gson gson = new Gson();
            final JsonReader reader = new JsonReader(new StringReader(gsonString));
            reader.setLenient(true);
            result.add(gson.fromJson(elem, cls));
        }
        return result;
    }

}
