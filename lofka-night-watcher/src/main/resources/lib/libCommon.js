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
 * JAVA路径的位置
 * @type {string}
 * @private
 */
__TOOL_JAVA_CLASS_PATH = "com.cvnavi.streaming.engine.tool";

/**
 * JAVA工具类的类型
 * @param tool_name
 * @returns {*} CLASS
 */
function get_tool_type(tool_name) {
    return Java.type(__TOOL_JAVA_CLASS_PATH + "." + tool_name);
}

var __ENGINE_TOOLS = get_tool_type("CommonTools");
var __GLOBAL_LOGGER = __ENGINE_TOOLS.createLogger("nashorn-vm-logger");

/**
 * 将 JavaScript的日志系统对接到Java使用的日志系统上
 * 从而可以对接到Lofka
 */
var console = {
    "logger": __GLOBAL_LOGGER,
    "log": function (info) {
        __GLOBAL_LOGGER.info(info);
    },
    "warn": function (info) {
        __GLOBAL_LOGGER.warn(info);
    },
    "err": function (info) {
        __GLOBAL_LOGGER.error(info);
    },
    "logJSON": function (info) {
        __GLOBAL_LOGGER.info(JSON.stringify(info, null, 2));
    },
    "warnJSON": function (info) {
        __GLOBAL_LOGGER.warn(JSON.stringify(info, null, 2));
    },
    "errJSON": function (info) {
        __GLOBAL_LOGGER.error(JSON.stringify(info, null, 2));
    }
};

/**
 * 将Java对象转换成JSON
 * @param java_obj
 * @returns {*}
 */
function java_object_to_json_string(java_obj) {
    return __ENGINE_TOOLS.toJson(java_obj);
}

/**
 * 将Java对象转换成JSON
 * @param java_obj
 * @returns {*}
 */
function java_object_to_json(java_obj) {
    return JSON.parse(java_object_to_json_string(java_obj));
}

var __global_eval = this.eval;

/**
 * 加载库函数
 * @param libname 库的名称
 */
function load_library(libname) {
    try {
        if (!libname.endsWith(".js")) {
            libname = libname + ".js";
        }
        __global_eval(
            __ENGINE_TOOLS.loadResourceFile(
                "lib/" + libname
            )
        );
    } catch (e) {
        console.err("Error while loading " + libname + " caused by: " + e.toString());
    }
}

var Thread = Java.type('java.lang.Thread');

/**
 * 创建线程
 * @param runnable_function 无参数、无返回函数
 * @returns {*}
 */
function create_thread(runnable_function) {
    return new Thread(runnable_function);
}

/**
 * 异步执行
 * @param runnable_function 无参数、无返回函数
 * @returns {*}
 */
function async_execute(runnable_function) {
    var thread = create_thread(runnable_function);
    thread.start();
    return thread;
}

/**
 * 休眠一定的时间
 * @param millseconds 毫秒数量
 */
function sleep(millseconds) {
    Thread.sleep(millseconds);
}