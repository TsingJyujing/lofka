package com.github.tsingjyujing.lofka.basic;

import com.github.tsingjyujing.lofka.util.NetUtil;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息异步发送程序，自动根据日志大小选择压缩或者不压缩的接口
 *
 * @author yuanyifan
 */
public class LoggerJsonAsyncAutoProcessor extends BaseAsyncProcessor<Map> {
    private final Gson gson = new Gson();
    private final String batchLogPostLocation;
    private final String batchLogCompressedPostLocation;

    public long getCompressLimit() {
        return compressLimit.get();
    }

    /**
     * 设置压缩触发极限
     * 一般是1k～2k左右触发比较合适
     * 如果服务器CPU宝贵且内网网速充足的话可以设置大一些
     *
     * @param value
     */
    public void setCompressLimit(long value) {
        compressLimit.set(value);
    }

    private final AtomicLong compressLimit = new AtomicLong(1024);

    /**
     * 自动推送初始化
     *
     * @param batchLogPostLocation           普通批量推送接口
     * @param batchLogCompressedPostLocation 压缩批量推送接口
     */
    public LoggerJsonAsyncAutoProcessor(
            String batchLogPostLocation,
            String batchLogCompressedPostLocation
    ) {
        this.batchLogPostLocation = batchLogPostLocation;
        this.batchLogCompressedPostLocation = batchLogCompressedPostLocation;
    }

    @Override
    protected void processData(List<Map> batchData) throws Exception {
        final String jsonData = gson.toJson(batchData);
        if (jsonData.length() > getCompressLimit()) {
            NetUtil.verifyResponse(
                    NetUtil.retryPostCompressedData(
                            batchLogCompressedPostLocation,
                            jsonData
                    )
            );
        } else {
            NetUtil.verifyResponse(
                    NetUtil.retryPost(
                            batchLogPostLocation,
                            jsonData
                    )
            );
        }

    }
}
