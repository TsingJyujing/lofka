package com.github.tsingjyujing.lofka.nightwatcher.util;

import com.github.tsingjyujing.lofka.basic.BaseClosableTimedTask;
import com.github.tsingjyujing.lofka.util.NetUtil;

import java.net.URI;
import java.util.function.Consumer;

/**
 * 监听URL改变
 */
public class UriListener extends BaseClosableTimedTask {

    private String lastValue = null;
    private long sleepInterval;
    private URI uri;

    /**
     * 新建一个监听器
     *
     * @param sleepInterval  监听间隔（毫秒）
     * @param uri            监听的URL
     * @param listenFunction 监听的函数
     */
    public UriListener(long sleepInterval, URI uri, Consumer<String> listenFunction) {
        this.sleepInterval = sleepInterval;
        this.uri = uri;
        this.listenFunction = listenFunction;
    }

    private Consumer<String> listenFunction;

    @Override
    protected void unitProgram() throws Exception {
        boolean trigger = false;
        String newValue = null;
        try {
            newValue = NetUtil.get(uri);
            trigger = !newValue.equals(lastValue);
        } catch (Exception ex) {
            if (lastValue != null) {
                trigger = true;
            }
        }
        if (trigger) {
            listenFunction.accept(newValue);
            lastValue = newValue;
        }
    }

    @Override
    protected long getSleepInterval() throws Exception {
        return sleepInterval;
    }
}


