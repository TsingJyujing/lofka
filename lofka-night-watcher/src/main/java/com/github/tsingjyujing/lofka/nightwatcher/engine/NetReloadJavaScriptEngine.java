package com.github.tsingjyujing.lofka.nightwatcher.engine;


import com.github.tsingjyujing.lofka.nightwatcher.basic.BaseNetAutoReloader;
import com.github.tsingjyujing.lofka.util.FileUtil;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JavaScript引擎（从指定URI读取脚本，自动更新）
 * <p>
 * 对于调用方法并行执行代码是线程安全的（但是同时只有一个线程）
 *
 * @author yuanyifan
 */
public class NetReloadJavaScriptEngine extends BaseNetAutoReloader {

    private final static String ENGINE_NOT_LOADED = "Engine hasn't initialized.";

    protected Logger LOGGER = LoggerFactory.getLogger(NetReloadJavaScriptEngine.class);

    private NashornScriptEngine engine;

    /**
     * 使用引擎
     *
     * @param engineRFunction 消费引擎的函数
     */
    public <R> R useEngine(Function<NashornScriptEngine, R> engineRFunction) {
        engineLock.lock();
        try {
            return engineRFunction.apply(engine);
        } finally {
            engineLock.unlock();
        }
    }

    /**
     * 使用引擎
     *
     * @param engineRFunction 消费引擎的函数
     */
    public void useEngine(Consumer<NashornScriptEngine> engineRFunction) {
        engineLock.lock();
        try {
            engineRFunction.accept(engine);
        } finally {
            engineLock.unlock();
        }
    }

    private final ReentrantLock engineLock = new ReentrantLock();

    /**
     * 初始化JavaScript引擎
     *
     * @param listenPath 监听URL
     */
    public NetReloadJavaScriptEngine(URI listenPath) {
        this(listenPath, 5);
    }

    /**
     * 初始化JavaScript引擎
     *
     * @param listenPath         监听URL
     * @param watchChangeSeconds 监控周期（不填写默认5秒）
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public NetReloadJavaScriptEngine(URI listenPath, int watchChangeSeconds) {
        super(listenPath, watchChangeSeconds * 1000);
        LOGGER.info("Started listening data {} changing from consul in period of {} seconds.", listenPath, watchChangeSeconds);
    }

    /**
     * 执行脚本，获得结果
     *
     * @param data 输入的数据，一般是JS格式的，具体的格式由业务定义
     * @return String格式，后续处理也依据文档定义
     */
    public Optional<String> processData(String data) {
        engineLock.lock();
        try {
            if (engine == null) {
                throw new RuntimeException(ENGINE_NOT_LOADED);
            }
            final Object result = engine.invokeFunction("processing_data", data);
            if (result != null) {
                if (result instanceof String) {
                    return Optional.of((String) result);
                } else {
                    LOGGER.warn("Function return a {} instead of java.lang.String", result.getClass().toString());
                    return Optional.of(result.toString());
                }
            } else {
                return Optional.<String>empty();
            }
        } catch (Throwable ex) {
            if (ENGINE_NOT_LOADED.equals(ex.getMessage())) {
                //pass
            } else {
                LOGGER.error("Error while running function", ex);
            }
            return Optional.<String>empty();
        } finally {
            engineLock.unlock();
        }
    }

    /**
     * 创建一个内置的Nashorn虚拟机
     *
     * @param initScript 初始化脚本
     * @return
     * @throws Exception
     */
    public static NashornScriptEngine createAnNewNashornEngine(final String initScript) throws Exception {
        NashornScriptEngine engine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();
        engine.eval(
                FileUtil.readTextResource(
                        "lib/libCommon.js"
                )
        );
        engine.eval(
                FileUtil.readTextResource(
                        "onload.js"
                )
        );
        engine.eval(initScript);
        engine.invokeFunction("open");
        return engine;
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    protected void reload(String initScript) {
        engineLock.lock();
        try {
            LOGGER.info("Loading engine for script:\n" + initScript);
            if (engine != null) {
                engine.invokeFunction("close");
            }
            engine = createAnNewNashornEngine(initScript);
        } catch (Throwable e) {
            LOGGER.error(
                    String.format(
                            "Error while loading code: \n %s",
                            initScript
                    ),
                    e
            );
        } finally {
            engineLock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        engineLock.lock();
        if (engine != null) {
            engine.invokeFunction("close");
        }
    }

    /**
     * 脚本运行
     */
    public class ScriptRunner implements Callable<Optional<String>> {

        private final String data;

        private ScriptRunner(String data) {
            this.data = data;
        }

        @Override
        public Optional<String> call() throws Exception {
            return processData(data);
        }
    }

    /**
     * 获取一个Callable[String] 来为多线程服务
     *
     * @param data
     * @return
     */
    public ScriptRunner getRunner(String data) {
        return new ScriptRunner(data);
    }
}
