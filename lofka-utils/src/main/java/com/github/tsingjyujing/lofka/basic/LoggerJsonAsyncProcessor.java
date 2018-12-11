package com.github.tsingjyujing.lofka.basic;

import com.github.tsingjyujing.lofka.util.NetUtil;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

/**
 * 消息异步发送程序
 *
 * @author yuanyifan
 */
@Deprecated
public class LoggerJsonAsyncProcessor extends BaseAsyncProcessor<Map> {
    private final Gson gson = new Gson();
    private final String batchLogPostLocation;

    /**
     * 初始化
     *
     * @param batchLogPostLocation 需要推送的URL
     */
    public LoggerJsonAsyncProcessor(String batchLogPostLocation) {
        this.batchLogPostLocation = batchLogPostLocation;
    }

    @Override
    protected void processData(List<Map> batchData) throws Exception {
        NetUtil.verifyResponse(
                NetUtil.retryPost(
                        batchLogPostLocation,
                        gson.toJson(batchData)
                )
        );
    }
}
