package com.github.tsingjyujing.lofka.persistence.source;

import com.github.tsingjyujing.lofka.persistence.basic.IBatchLoggerProcessor;
import com.github.tsingjyujing.lofka.persistence.basic.ILogReceiver;
import com.google.common.collect.Maps;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Collection;
import java.util.Map;

/**
 * 本地数据注册中心
 * 服务器接受到的日志都吐到这里（不一定完整）
 */
public class LocalWriterRegister implements IBatchLoggerProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(LocalWriterRegister.class);

    private static LocalWriterRegister ourInstance = new LocalWriterRegister();

    public static LocalWriterRegister getInstance() {
        return ourInstance;
    }

    private final Map<String, ILogReceiver> registerInfo = Maps.newHashMap();

    private LocalWriterRegister() {

    }

    /**
     * 注册处理器
     *
     * @param name      名称
     * @param processor 处理器
     */
    public void register(String name, ILogReceiver processor) {
        synchronized (registerInfo) {
            if (registerInfo.containsKey(name)) {
                throw new KeyAlreadyExistsException("Processor " + name + " already registered.");
            } else {
                registerInfo.put(name, processor);
                LOGGER.info("Receiver {} registered, class={}", name, processor.getClass().toString());
            }
        }
    }

    /**
     * 解除注册
     *
     * @param name 名称
     */
    public void unregister(String name) {
        synchronized (registerInfo) {
            if (registerInfo.containsKey(name)) {
                ILogReceiver processor = registerInfo.remove(name);
                LOGGER.info("Receiver {} unregistered, class={}", name, processor.getClass().toString());
            } else {
                throw new RuntimeException(name + " not registered!");
            }
        }
    }

    /**
     * 处理日志
     *
     * @param logs log in LoggerJson format
     */
    @Override
    public void processLoggers(Collection<Document> logs) {
        synchronized (registerInfo) {
            for (ILogReceiver processor : registerInfo.values()) {
                try {
                    processor.processLoggers(logs);
                } catch (Exception e) {
                    LOGGER.error("Error while pushing logs:", e);
                }
            }
        }
    }

}
