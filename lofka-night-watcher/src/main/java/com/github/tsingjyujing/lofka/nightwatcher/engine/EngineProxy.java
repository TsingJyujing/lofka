package com.github.tsingjyujing.lofka.nightwatcher.engine;

import org.apache.flink.api.common.ExecutionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;


/**
 * 代理引擎
 */
public class EngineProxy implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineProxy.class);
    private final ConsulConnectInfo consulConnectInfo;

    public EngineProxy(ConsulConnectInfo consulConnectInfo) {
        this.consulConnectInfo = consulConnectInfo;
    }

    public EngineManager.EngineSession getEngineSession() throws URISyntaxException {
        return EngineManager.getInstance().getEngines(consulConnectInfo);
    }

    public Map<String, Optional<String>> executeAll(String data) throws URISyntaxException {
        return EngineManager.getInstance().getEngines(consulConnectInfo).executeAll(data);
    }


}
