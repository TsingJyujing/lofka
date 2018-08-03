package com.github.tsingjyujing.lofka.server;

import org.junit.Test;
public class MessageGenerator {

    private static final org.slf4j.Logger logbackLogger = org.slf4j.LoggerFactory.getLogger("logger-slf4j");
    private static final org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger("logger-log4j");
    private static final org.apache.logging.log4j.Logger log4j2Logger = org.apache.logging.log4j.LogManager.getLogger("logger-log4j2");

    @Test
    public void generateMessages() throws Exception{
        while (true) {
            Thread.sleep(1000);

            System.out.println("start to log:");

            logbackLogger.info("Fun info");
            log4jLogger.info("Fun info");
            log4j2Logger.info("Fun info");

            Thread.sleep(1000);

            logbackLogger.warn("Fun warn");
            log4jLogger.warn("Fun warn");

            log4j2Logger.warn("Fun warn");

            Thread.sleep(1000);

            try {
                testFunL1();
            } catch (Exception ex) {
                logbackLogger.error("Fun failed", ex);
                log4jLogger.error("Fun failed", ex);
                log4j2Logger.error("Fun failed", ex);
            }

        }
    }

    private static void testFunL1() {
        testFunL2();
    }

    private static void testFunL2() {
        testFunL3();
    }

    private static void testFunL3() {
        testFunL4();
    }

    private static void testFunL4() {
        throw new RuntimeException("Exception Test");
    }
}
