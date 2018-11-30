/*
 * Copyright 1993-2018 TsingJyujing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

load_library("libHttp");
load_library("libMongoDB");
load_library("libRedis");
load_library("libSQL");

// 加载默认脚本
/**
 * 加载虚拟机的时候执行
 */
function open() {

}

/**
 * 关闭虚拟机的时候执行
 * 一般用于关闭连接
 */
function close() {

}

/**
 * 处理数据
 */
function processing_data(data_str) {
    throw Error("Processing function undefined");
}

