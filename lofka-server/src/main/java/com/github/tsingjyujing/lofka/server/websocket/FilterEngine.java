package com.github.tsingjyujing.lofka.server.websocket;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.InputStreamReader;

/**
 * JS过滤引擎
 * @author yuanyifan
 */
public class FilterEngine {
    private final static Logger LOGGER = LoggerFactory.getLogger(FilterEngine.class);
    private final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
    private Invocable invocable;

    private boolean isInitialized;

    /**
     * 创建脚本引擎
     *
     * @throws ScriptException
     */
    public FilterEngine() {
        try {
            engine.eval(new InputStreamReader(FilterEngine.class.getResourceAsStream("/filter.js")));
            invocable = (Invocable) engine;
            isInitialized = true;
        } catch (Exception ex) {
            LOGGER.error("Error while initializing NashornVM.", ex);
            isInitialized = false;
        }
    }

    /**
     * 获得过滤结果
     *
     * @param message
     * @return
     */
    public boolean getFilterResult(final String message) {
        if (isInitialized) {
            try {
                return (Boolean) invocable.invokeFunction("invoke_filter",message);
            } catch (Exception ex) {
                LOGGER.warn("Error while filtering message:", ex);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 设置过滤器
     *
     * @param script
     * @throws ScriptException
     */
    public void setFilter(final String script) throws ScriptException {
        engine.eval(script);
    }

}
