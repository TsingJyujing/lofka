/*
 * Copyright 1993-2018 TsingJyujing
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tsingjyujing.lofka.nightwatcher.config;

import com.github.tsingjyujing.lofka.basic.BaseClosableTimedTask;
import com.github.tsingjyujing.lofka.nightwatcher.basic.HostAndPort;
import com.github.tsingjyujing.lofka.nightwatcher.util.UriListener;
import com.github.tsingjyujing.lofka.util.NetUtil;
import com.google.common.collect.Sets;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

/**
 * 和Consul交互的一些函数和方法
 */
public class ConsulConnection {

    /**
     * 初始化连接
     *
     * @param hosts Consul主机信息
     */
    public ConsulConnection(Collection<HostAndPort> hosts) {
        getHosts().addAll(Sets.newHashSet(hosts));
    }

    /**
     * 初始化连接
     *
     * @param hosts Consul主机信息
     * @param token Token信息
     */
    public ConsulConnection(Collection<HostAndPort> hosts, String token) {
        this(Sets.newHashSet(hosts));
        setToken(token);
    }

    private final List<HostAndPort> hosts = new ArrayList<>();

    public List<HostAndPort> getHosts() {
        return hosts;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String token = null;

    private final Random rand = new Random();

    /**
     * 获取数值
     *
     * @param path 位置
     * @return
     */
    public Optional<String> queryValueAsString(String path) {
        try {
            return Optional.of(NetUtil.get(generateUriByPath(path)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 生成获取
     *
     * @param path
     * @return
     */
    public URI generateUriByPath(String path) throws URISyntaxException {
        HostAndPort selected = randomPickHost();
        final URIBuilder builder = new URIBuilder(
                String.format(
                        "http://%s:%d/v1/kv/%s",
                        selected.getHost(),
                        selected.getPort(),
                        path
                )
        );
        if (getToken() != null) {
            // fixme set token in header lead more safe
            builder.setParameter("token", getToken());
        }
        builder.setParameter("raw", "");
        return builder.build();
    }

    /**
     * 随机选中一个机器
     *
     * @return
     */
    private HostAndPort randomPickHost() {
        return hosts.get(rand.nextInt(hosts.size()));
    }


    /**
     * 创建监听器
     *
     * @param sleepInterval 监听间隔（毫秒）
     * @param listenKey     监听的键
     * @param listener      监听到新数值的处理函数
     * @return
     * @throws URISyntaxException
     */
    public BaseClosableTimedTask createListener(final long sleepInterval, String listenKey, Consumer<String> listener) throws URISyntaxException {
        return new UriListener(sleepInterval, generateUriByPath(listenKey), listener);
    }
}
