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


/**
 * Http GET 的实现
 * @param url URL
 * @param data 需要附加发送的数据
 * @param callback_function 收到数据的回调函数
 * @private
 */
function __http_get(url, data, callback_function) {
    return callback_function(__ENGINE_TOOLS.get(url, JSON.stringify(data)));
}

/**
 * Http POST 的实现
 * @param url URL
 * @param data 需要附加发送的数据
 * @param callback_function 收到数据的回调函数
 * @private
 */
function __http_post(url, data, callback_function) {
    if (typeof(data) === "string") {
        return callback_function(__ENGINE_TOOLS.postString(url, data, "text/plain"));
    } else {
        return callback_function(__ENGINE_TOOLS.postUrlencoded(url, JSON.stringify(data)));
    }
}

/**
 * 异步 Http GET 的实现
 * @param url URL
 * @param data 需要附加发送的数据
 * @param callback_function 收到数据的回调函数
 * @private
 */
function __http_get_async(url, data, callback_function) {
    async_execute(function () {
        callback_function(__ENGINE_TOOLS.get(url, JSON.stringify(data)))
    })
}

/**
 * 异步 Http POST 的实现
 * @param url URL
 * @param data 需要附加发送的数据
 * @param callback_function 收到数据的回调函数
 * @private
 */
function __http_post_async(url, data, callback_function) {
    async_execute(function () {
        if (typeof(data) === "string") {
            return callback_function(__ENGINE_TOOLS.postString(url, data, "text/plain"));
        } else {
            return callback_function(__ENGINE_TOOLS.postUrlencoded(url, JSON.stringify(data)));
        }
    })
}

/**
 * 模仿jQuery的API，区别是这里不少方法都是同步阻塞的，有返回值
 */
var syncHttp = {
    "get": __http_get,
    "post": __http_post,
    "getJSON": function (url, data, callback_function) {
        return __http_get(
            url, data, function (str_data) {
                callback_function(JSON.parse(str_data))
            }
        );
    }
};

/**
 * 模仿JQuery的写法，这里都是异步的，无返回值
 */
var asyncHttp = {
    "get": __http_get_async,
    "post": __http_post_async,
    "getJSON": function (url, data, callback_function) {
        return __http_get_async(
            url, data, function (str_data) {
                callback_function(JSON.parse(str_data))
            }
        );
    }
};

/**
 * 兼容jQuery
 */
var $ = asyncHttp;