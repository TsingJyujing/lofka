package com.github.tsingjyujing.lofka.nightwatcher.engine;

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

    /**
     * Make POJO happy
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    private final String url;

    public EngineProxy(String url) {
        this.url = url;
    }

    public EngineManager.EngineSession getEngineSession() throws URISyntaxException {
        return EngineManager.getInstance().getEngines(url);
    }

    public Map<String, Optional<String>> executeAll(String data) throws URISyntaxException {
        return EngineManager.getInstance().getEngines(url).executeAll(data);
    }


}
