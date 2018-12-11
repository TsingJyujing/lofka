package com.github.tsingjyujing.lofka.server;

import org.junit.Test;

public class MessagePressureGenerator {

    private static final org.slf4j.Logger logbackLogger = org.slf4j.LoggerFactory.getLogger("logger-pressure");

    @Test
    public void pressureTest() throws Exception {
        System.out.println("start to log");
        long startTick = System.currentTimeMillis();
        int dataCount = 1000000;
        for (int i = 0; i <= dataCount; i++) {
            logbackLogger.info("Test message on pressure.");
        }
        System.out.printf("Log %s data in %d ms\n", dataCount, System.currentTimeMillis() - startTick);
    }
}
