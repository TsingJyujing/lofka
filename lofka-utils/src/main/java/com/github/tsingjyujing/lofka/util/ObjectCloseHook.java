package com.github.tsingjyujing.lofka.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于关闭对象的进程
 *
 * @author yuanyifan
 */
public class ObjectCloseHook implements Runnable {

    private final Logger LOGGER;
    private final AutoCloseable obj;

    public ObjectCloseHook(AutoCloseable obj) {
        LOGGER = LoggerFactory.getLogger(obj.getClass());
        this.obj = obj;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Closing self...");
            obj.close();
            LOGGER.info("Self closed successfully.");
        } catch (Exception e) {
            LOGGER.error("Error while closing self:", e);
        }
    }

    /**
     * 增加在关闭程序的时候，自动关闭对象。
     *
     * @param obj 需要关闭的对象
     */
    public static void addCloseObjectHook(AutoCloseable obj) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        new ObjectCloseHook(
                                obj
                        )
                )
        );
    }
}