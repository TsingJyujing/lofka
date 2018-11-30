package com.github.tsingjyujing.lofka.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 可关闭的定时任务基础类
 *
 * @author yuanyifan
 */
public abstract class BaseClosableTimedTask implements Runnable, AutoCloseable {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
    protected final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final AtomicLong recheckDuration = new AtomicLong(50);

    /**
     * 设置重新检查周期
     * 必需在1~750ms之间，否则容易关闭异常
     * 除非CPU非常吃紧了，否则推荐设置检查周期在20ms(50Hz)~50ms(20Hz)之间
     * 周期设置基本不影响睡眠时间
     *
     * @param recheckDuration 重新检查的周期，单位ms
     */
    public void setRecheckDuration(long recheckDuration) {
        if (recheckDuration > 0 && recheckDuration < 750) {
            this.recheckDuration.set(recheckDuration);
        }
    }

    /**
     * 被定时运行的程序
     *
     * @throws Exception
     */
    protected abstract void unitProgram() throws Exception;

    /**
     * 运行间隔
     *
     * @return
     * @throws Exception
     */
    protected abstract long getSleepInterval() throws Exception;


    public void shutdown() {
        LOGGER.info("{} closing.", getClass().getName());
        isClosed.set(true);
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }

    /**
     * 可关闭的定时任务
     */
    public BaseClosableTimedTask() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {
                    LOGGER.error("Force exited", ex);
                }
            }
        }));
    }

    @Override
    public void run() {
        long lastRanTick = 0;
        while (!isClosed.get()) {
            try {
                final long deltaTick = System.currentTimeMillis() - lastRanTick;
                if (deltaTick > getSleepInterval()) {
                    lastRanTick = System.currentTimeMillis();
                    unitProgram();
                    LOGGER.debug("{} ran once normally.", getClass().getName());
                } else {
                    Thread.sleep(recheckDuration.get());
                }
            } catch (InterruptedException iex) {
                break;
            } catch (Throwable ex) {
                LOGGER.error(String.format("Error while %s iterating.", getClass().getName()), ex);
            }
        }
        LOGGER.info("{} closed normally.", getClass().getName());
    }
}
