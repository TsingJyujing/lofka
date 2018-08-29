package com.github.tsingjyujing.lofka.task;

import com.github.tsingjyujing.lofka.basic.BaseClosableTimedTask;
import com.github.tsingjyujing.lofka.util.DocumentUtil;
import com.google.gson.Gson;

/**
 * Lofka 心跳机制
 *
 * @author yuanyifan
 */
public class HeartBeat extends BaseClosableTimedTask {
    private final static Gson GSON = new Gson();

    @Override
    protected void unitProgram() throws Exception {
        LOGGER.debug(DocumentUtil.generateHeartBeatMessage(getHeartBeatName(), getSleepInterval()));
    }

    @Override
    public long getSleepInterval() {
        return sleepInterval;
    }

    /**
     * 心跳包间隔
     */
    private final long sleepInterval;


    public String getHeartBeatName() {
        return heartBeatName;
    }

    /**
     * 心跳包名称
     */
    private final String heartBeatName;


    /**
     * 初始化心跳包进程
     *
     * @param heartBeatName 心跳包名称
     * @param sleepInterval 心跳间隔
     */
    public HeartBeat(String heartBeatName, long sleepInterval) {
        super();
        this.sleepInterval = sleepInterval;
        this.heartBeatName = heartBeatName;
    }

    /**
     * 初始化心跳包进程
     *
     * @param heartBeatName 心跳包名称
     */
    public HeartBeat(String heartBeatName) {
        this(heartBeatName, 1000);
    }


}
