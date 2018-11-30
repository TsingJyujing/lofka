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

var __SQL_TOOLS = get_tool_type("SQLConnectionWrapper");

/**
 * SQL连接的封装
 * @param setting
 * @constructor
 */
function SQLConnection(setting) {
    this.__conn = __SQL_TOOLS.createFromJsonString(JSON.stringify(setting));
}

SQLConnection.prototype = {
    /**
     * 执行查询
     * @param sql SQL
     * @returns {any} 对返回结果的一个封装
     */
    "query": function (sql) {
        return JSON.parse(
            this.__conn.query(
                sql
            ).toJson()
        );
    },
    /**
     * 执行无返回结果的SQL语句
     * @param sql SQL
     */
    "exec": function (sql) {
        this.__conn.executeSQL(
            sql
        )
    },
    "close": function () {
        this.__conn.close();
        this.__conn = null;
    }
};