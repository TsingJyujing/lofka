package com.github.tsingjyujing.lofka.nightwatcher.engine;

import com.github.tsingjyujing.lofka.nightwatcher.basic.BaseNetAutoReloader;
import com.github.tsingjyujing.lofka.util.ObjectCloseHook;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 用于管理VM集群的地方
 */
public class EngineManager implements AutoCloseable {

    private static final int watchSeconds = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineManager.class);

    volatile private static EngineManager ourInstance = null;

    private final HashMap<ConsulConnectInfo, EngineSession> engineSessions = Maps.newHashMap();

    /**
     * 获取经过实例化的单例，向毛主席保证线程安全
     *
     * @return HBase连接管理器实例
     */
    public static EngineManager getInstance() {
        if (ourInstance == null) {
            synchronized (EngineProxy.class) {
                if (ourInstance == null) {
                    try {
                        ourInstance = new EngineManager(
                                Math.max(Runtime.getRuntime().availableProcessors() - 2, 1)
                        );
                    } catch (Exception ex) {
                        LOGGER.error("Error while initializing HBase connection", ex);
                    }
                }
            }
        }
        return ourInstance;
    }

    private final ThreadPoolExecutor scriptEngineRunningPool;

    /**
     * 初始化连接管理器
     *
     * @param queryConcurrency 并发数量
     */
    private EngineManager(int queryConcurrency) {
        final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("script-engine-run-%d").build();
        scriptEngineRunningPool = new ThreadPoolExecutor(
                queryConcurrency, Integer.MAX_VALUE,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
        ObjectCloseHook.addCloseObjectHook(this);
    }

    /**
     * 通过连接信息获取执行引擎
     *
     * @param key 连接信息
     * @return 连接会话
     */
    public EngineSession getEngines(ConsulConnectInfo key) throws URISyntaxException {
        synchronized (engineSessions) {
            if (!engineSessions.containsKey(key)) {
                engineSessions.put(key, new EngineSession(key));
            }
            return engineSessions.get(key);
        }
    }

    @Override
    public void close() throws Exception {
        scriptEngineRunningPool.shutdown();
    }


    /**
     * 用于存储某个Consul会话下的所有JS引擎
     */
    public class EngineSession extends BaseNetAutoReloader {

        private final HashMap<String, NetReloadJavaScriptEngine> engines = Maps.newHashMap();
        private final ConsulConnectInfo consulConnectInfo;

        public EngineSession(ConsulConnectInfo consulConnectInfo) throws URISyntaxException {
            super(
                    consulConnectInfo.createConsulClient().generateUriByPath(
                            consulConnectInfo.getPath() + "scripts/config.json"
                    ),
                    watchSeconds * 1000
            );
            this.consulConnectInfo = consulConnectInfo;
        }

        /**
         * 执行所有命令并且返回数据
         *
         * @param data 输入的数据
         * @return 返回的数据
         */
        public Map<String, Optional<String>> executeAll(String data) {
            final List<Tuple2<String, Future<Optional<String>>>> futureResults = Lists.newArrayList();
            final Map<String, Optional<String>> results = Maps.newHashMap();
            synchronized (engines) {
                for (Map.Entry<String, NetReloadJavaScriptEngine> engineEntry : engines.entrySet()) {
                    futureResults.add(
                            new Tuple2<>(
                                    engineEntry.getKey(),
                                    scriptEngineRunningPool.submit(engineEntry.getValue().getRunner(data))
                            )
                    );
                }
            }
            for (Tuple2<String, Future<Optional<String>>> futureResult : futureResults) {
                try {
                    results.put(futureResult._1(), futureResult._2().get());
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Error while executing script:", e);
                }
            }
            return results;
        }


        /**
         * 根据Consul上的配置文件重新整理JS引擎
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void reload(String newValue) {
            LOGGER.info("(Re)Loading engines....");
            final Set<String> fileNames = Sets.newHashSet(
                    ((ArrayList<String>) new Gson().fromJson(
                            newValue,
                            new TypeToken<ArrayList<String>>() {
                            }.getType()
                    )).stream().map(
                            filename -> consulConnectInfo.getPath() + "scripts/" + filename
                    ).iterator()
            );
            LOGGER.info("There're {} script(s) attempted to run.", fileNames.size());
            synchronized (engines) {
                for (String fileName : fileNames) {
                    if (!engines.containsKey(fileName)) {
                        try {
                            engines.put(
                                    fileName,
                                    new NetReloadJavaScriptEngine(
                                            new URI(
                                                    consulConnectInfo.getPath() + "scripts/" + fileName
                                            ),
                                            watchSeconds * 1000
                                    )
                            );
                        } catch (Exception e) {
                            LOGGER.error("Error while loading VM caused by:", e);
                        }
                    }
                }
                Set<String> currentFileNames = Sets.newHashSet(engines.keySet());
                for (String fileName : currentFileNames) {
                    if (!fileNames.contains(fileName)) {
                        engines.remove(fileName);
                    }
                }
            }
        }

        @Override
        public void close() throws Exception {
            synchronized (engines) {
                for (Map.Entry<String, NetReloadJavaScriptEngine> engineEntry : engines.entrySet()) {
                    engineEntry.getValue().close();
                }
                engines.clear();
            }
        }
    }

}
