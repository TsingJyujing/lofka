package com.github.tsingjyujing.lofka.nightwatcher.basic;

import com.github.tsingjyujing.lofka.nightwatcher.util.UriListener;

import java.net.URI;

/**
 * 当有新数据的时候自动从网络加载
 */
public abstract class BaseNetAutoReloader implements AutoCloseable {

    public UriListener getListener() {
        return listener;
    }

    private final UriListener listener;

    /**
     * 初始化加载器
     *
     * @param listenUri     需要监听的URI
     * @param watchInterval 监听间隔
     */
    public BaseNetAutoReloader(URI listenUri, long watchInterval) {
        listener = new UriListener(watchInterval, listenUri, this::reload);
        new Thread(listener).start();
    }

    /**
     * 重新加载的动作
     *
     * @param newValue 新的数值
     */
    protected abstract void reload(String newValue);

    @Override
    public void close() throws Exception {
        listener.close();
    }
}
