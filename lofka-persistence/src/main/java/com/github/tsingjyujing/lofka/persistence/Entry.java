package com.github.tsingjyujing.lofka.persistence;

import com.github.tsingjyujing.lofka.persistence.basic.ILogReceiver;
import com.github.tsingjyujing.lofka.persistence.util.ConfigLoader;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 持久化入口
 */
public class Entry {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Entry.class);

    /**
     * @param args 不需要参数
     */
    public static void main(String[] args) throws Exception {
        runServices("lofka-persistence.json", false);
    }

    /**
     * 负责初始化所有持久化服务并且开始运行
     *
     * @param configFilename 配置文件名称
     * @throws Exception
     */
    public static void runServices(String configFilename, boolean nonBlock) throws Exception {
        final ArrayList<ILogReceiver> sourceList = ConfigLoader.loadSource(configFilename);

        final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("persistence-%d").build();

        final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                sourceList.size(),
                Integer.MAX_VALUE,
                100000L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                namedThreadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        for (ILogReceiver source : sourceList) {
            threadPool.execute(source);
        }

        threadPool.shutdown();
        LOGGER.info("Persistence service(s) is running...");

        if (!nonBlock) {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            LOGGER.error("Persistence service(s) terminated abnormally");
        }
    }
}
