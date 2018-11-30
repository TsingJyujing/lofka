package com.github.tsingjyujing.lofka.nightwatcher.engine;

import com.github.tsingjyujing.lofka.nightwatcher.basic.HostAndPort;
import com.github.tsingjyujing.lofka.nightwatcher.config.ConsulConnection;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

/**
 * Consul连接信息
 */
public class ConsulConnectInfo {

    public Set<HostAndPort> getHosts() {
        return hosts;
    }

    private final Set<HostAndPort> hosts = Sets.newHashSet();
    /**
     * 读取配置的基础路径
     */
    private final String path;


    private String token;

    /**
     * Consul连接信息
     *
     * @param hosts 连接地址
     * @param path  基础路径（会强制以"/"结尾）
     */
    public ConsulConnectInfo(
            Collection<HostAndPort> hosts,
            String path) {
        if (hosts.isEmpty()) {
            throw new RuntimeException("At lease one host info.");
        }
        this.hosts.addAll(hosts);
        if (path.endsWith("/")) {
            this.path = path;
        } else {
            this.path = path + "/";
        }
    }

    /**
     * Consul连接信息
     *
     * @param hosts 连接地址
     * @param path  基础路径（会强制以"/"结尾）
     * @param token 令牌
     */
    public ConsulConnectInfo(
            Collection<HostAndPort> hosts,
            String path,
            String token
    ) {
        this(
                hosts, path
        );
        setToken(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return hosts.stream().map(
                HostAndPort::hashCode
        ).reduce(
                (a, b) -> a ^ b
        ).orElse(
                0
        ) ^ path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConsulConnectInfo) {
            ConsulConnectInfo cmp = (ConsulConnectInfo) obj;
            if (cmp.path.equals(path) && cmp.hosts.size() == hosts.size()) {
                return hosts.containsAll(cmp.hosts);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 从自身的信息创建连接
     *
     * @return
     */
    public ConsulConnection createConsulClient() {
        return createConsulClient(this);
    }

    /**
     * 从Consul连接信息中创建连接
     *
     * @param consulConnectInfo 连接信息
     * @return
     */
    public static ConsulConnection createConsulClient(ConsulConnectInfo consulConnectInfo) {
        ConsulConnection conn = new ConsulConnection(consulConnectInfo.getHosts());
        if (consulConnectInfo.getToken() != null) {
            conn.setToken(consulConnectInfo.getToken());
        }
        return conn;
    }
}
