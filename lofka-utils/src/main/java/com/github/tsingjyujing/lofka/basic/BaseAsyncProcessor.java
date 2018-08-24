package com.github.tsingjyujing.lofka.basic;


import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步数据处理器
 * <p>
 * 支持时间触发和条数触发模式，满足任一触发条件即启动处理程序
 *
 * @param <DataType>
 * @author yuanyifan
 */
public abstract class BaseAsyncProcessor<DataType> {

    /**
     * 检查周期
     */
    private final AtomicInteger sleepDuration = new AtomicInteger(1000);

    public int getSleepDuration() {
        return sleepDuration.get();
    }

    public void setSleepDuration(int sleepDuration) {
        if (sleepDuration > 0) {
            this.sleepDuration.set(sleepDuration);
        }
        // else {throw new InvalidParameterException("Invalid sleep duration (should grater than 0): " + Integer.toString(sleepDuration)); }
    }

    /**
     * 最大缓冲区大小
     */
    private final AtomicInteger maxBufferSize = new AtomicInteger(1000);

    public void setMaxBufferSize(int size) {
        if (size > 0) {
            maxBufferSize.set(size);
        }
    }

    public int getMaxBufferSize() {
        return maxBufferSize.get();
    }

    private final LinkedList<DataType> dataBuffer = new LinkedList<>();

    /**
     * 添加数据
     *
     * @param data
     */
    public void offerData(DataType data) {
        if (data != null) {
            synchronized (dataBuffer) {
                dataBuffer.offer(data);
            }
            if (dataBuffer.size() > getMaxBufferSize()) {
                cleanBufferList();
            }
        }
    }

    private final BufferMonitor monitor = new BufferMonitor();

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public BaseAsyncProcessor() {
        new Thread(monitor).start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                monitor.stop();
            }
        }));
    }

    private void cleanBufferList() {
        final List<DataType> bufferCopy;
        synchronized (dataBuffer) {
            bufferCopy = Lists.newArrayList(dataBuffer);
            dataBuffer.clear();
        }
        if (!bufferCopy.isEmpty()) {
            try {
                processData(bufferCopy);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 批量数据处理
     *
     * @param batchData 批量数据
     * @throws Exception
     */
    protected abstract void processData(List<DataType> batchData) throws Exception;

    /**
     * 数据情况监控
     */
    private class BufferMonitor implements Runnable {

        private final AtomicBoolean isStopped = new AtomicBoolean(false);

        /**
         * 停止监控
         */
        public void stop() {
            isStopped.set(true);
        }

        @Override
        public void run() {
            while (!isStopped.get()) {
                if (dataBuffer.isEmpty()) {
                    try {
                        Thread.sleep(getSleepDuration());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    cleanBufferList();
                }
            }
            if (!dataBuffer.isEmpty()) {
                cleanBufferList();
            }
        }
    }
}
