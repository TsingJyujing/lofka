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

var __REDIS_CLUSTER_TOOLS = get_tool_type("RedisClusterWrapper");

/**
 * 初始化一个Redis集群
 * @param hosts
 * @constructor
 */
function RedisCluster(hosts) {
    this.__conn = __REDIS_CLUSTER_TOOLS.createFromJsonString(
        JSON.stringify({
            "hosts": hosts.join(",")
        })
    );
}

RedisCluster.prototype = {
    /**
     * 获取Java的连接对象直接操作
     * 不推荐使用这种方式获取
     * 应该在下面增加需要的方法
     * @returns {*|null} redis.clients.jedis.JedisCluster 对象
     */
    "get_connection": function () {
        return this.__conn.getJedisCluster();
    },
    "close": function () {
        this.__conn.close();
        this.__conn = null;
    }
};

/**
 * 这里都是基本的Redis操作，详细参阅官网
 */

RedisCluster.prototype.hget = function (key, field) {
    return this.get_connection().hget(key, field);
};

RedisCluster.prototype.hgetall = function (key) {
    return java_object_to_json(
        this.get_connection().hgetAll(key)
    );
};

RedisCluster.prototype.hmset = function (key, data) {
    return this.get_connection().hmset(key, __ENGINE_TOOLS.stringMapFromJson(JSON.stringify(data)));
};

RedisCluster.prototype.expire = function (key, seconds) {
    return this.get_connection().expire(key, seconds);
};

RedisCluster.prototype["set"] = function (key, value) {
    return this.get_connection().set(key, value);
};

RedisCluster.prototype.get = function (key) {
    return this.get_connection().get(key);
};

// todo 扩展更多的Redis操作（如果有需要的话）