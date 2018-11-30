package com.github.tsingjyujing.lofka.nightwatcher.engine.tool;

import org.bson.Document;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 连接到Redis集群的工具类
 */
public class RedisClusterWrapper implements AutoCloseable {
    /**
     * 获取连接
     * @return
     */
    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    private final JedisCluster jedisCluster;

    @Override
    public void close() throws Exception {
        jedisCluster.close();
    }

    /**
     * 初始化Redis连接池
     *
     * @param config
     */
    public RedisClusterWrapper(Properties config) {
        final Set<HostAndPort> hosts = new HashSet<>();
        final String[] hostsRaw = config.getProperty("hosts").split(",");
        for (String hostInfo : hostsRaw) {
            if (hostInfo.contains(":")) {
                final String[] hostAndPort = hostInfo.split(":");
                hosts.add(new HostAndPort(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
            } else {
                hosts.add(new HostAndPort(hostInfo, 6379));
            }
        }
        jedisCluster = new JedisCluster(hosts);
    }

    /**
     * 通过JSON格式的配置信息创建连接代理
     *
     * @param jsonSettings JSON配置，和Properties差不多
     * @return
     */
    public static RedisClusterWrapper createFromJsonString(String jsonSettings) {
        final Properties prop = new Properties();
        prop.putAll(Document.parse(jsonSettings));
        return new RedisClusterWrapper(prop);
    }

}
