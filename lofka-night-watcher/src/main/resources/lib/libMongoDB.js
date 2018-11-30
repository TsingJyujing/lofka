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

var __MONGODB_CLUSTER_TOOLS = get_tool_type("MongoDBWrapper");

var ArrayList = Java.type('java.util.ArrayList');
var Document = Java.type('org.bson.Document');
var Lists = Java.type('com.google.common.collect.Lists');

/**
 * 连接萌阔数据库
 * @param setting 萌阔数据库配置
 * @constructor
 */
function MongoClient(setting) {
    this.__conn = __MONGODB_CLUSTER_TOOLS.createFromJsonString(JSON.stringify(setting));
}

MongoClient.prototype = {
    "get_collection": function (db, coll) {
        return new MongoCollection(
            this.__conn.getCollection(db, coll)
        );
    },
    /**
     * 获取Java的MongoClient对象
     * @returns {*}
     */
    "get_connection": function () {
        return this.__conn().getConnection();
    },
    "close": function () {
        this.__conn.close();
        this.__conn = null;
    }
};

/**
 * 连接萌阔数据库的Collection
 * @param coll
 * @constructor
 */
function MongoCollection(coll) {
    this.__coll = coll;
}

/**
 * 注释略，都是
 * https://docs.mongodb.com/manual/reference/
 * 中描述的标准操作
 * 需要注意的是，不支持原生的BSON，而是支持JSON，有些地方需要转义。
 */
MongoCollection.prototype = {
    "find": function (filter) {
        return new MongoIterable(
            this.__coll.find(Document.parse(JSON.stringify(filter)))
        );
    },
    "count": function (filter) {
        return new MongoIterable(
            this.__coll.count(Document.parse(JSON.stringify(filter)))
        );
    },
    "aggregate": function (pipeline) {
        var pipelineInJava = new ArrayList();
        pipeline.forEach(function (stage) {
            pipelineInJava.add(Document.parse(JSON.stringify(stage)));
        });
        return new MongoIterable(this.__coll.aggregate(pipelineInJava)).toArray();
    },
    "remove": function (filter) {
        return new MongoIterable(
            this.__coll.deleteMany(Document.parse(JSON.stringify(filter))).getDeletedCount()
        );
    },
    "insert": function (data) {
        if (data instanceof Array) {
            var dataInJava = new ArrayList();
            data.forEach(function (x) {
                dataInJava.add(Document.parse(JSON.stringify(x)));
            });
            this.__coll.insertMany(dataInJava);
        } else {
            this.__coll.insertOne(Document.parse(JSON.stringify(data)));
        }
    },
    "upsert_one": function (filter, data) {
        __MONGODB_CLUSTER_TOOLS.upsertOne(
            this.__coll,
            Document.parse(JSON.stringify(filter)),
            Document.parse(JSON.stringify(data))
        );
    }
};

/**
 * 承载萌阔数据库返回数据的容器
 * @param data MongoIterable
 * @constructor
 */
function MongoIterable(data) {
    this.__data = data;
}


/**
 * 注释略，都是
 * https://docs.mongodb.com/manual/reference/
 * 中描述的标准操作
 * 需要注意的是，不支持原生的BSON，而是支持JSON，有些地方需要转义。
 */
MongoIterable.prototype = {
    "first": function () {
        return JSON.parse(this.__data.first().toJson());
    },
    "hint": function (bsonOption) {
        return new MongoIterable(this.__data.hint(Document.parse(JSON.stringify(bsonOption))))
    },
    "projection": function (bsonOption) {
        return new MongoIterable(this.__data.projection(Document.parse(JSON.stringify(bsonOption))))
    },
    "filter": function (bsonOption) {
        return new MongoIterable(this.__data.filter(Document.parse(JSON.stringify(bsonOption))))
    },
    "sort": function (bsonOption) {
        return new MongoIterable(this.__data.sort(Document.parse(JSON.stringify(bsonOption))))
    },
    "limit": function (limit) {
        return new MongoIterable(this.__data.limit(limit))
    },
    "skip": function (skip) {
        return new MongoIterable(this.__data.skip(skip))
    },
    "toArray": function () {
        return Java.from(
            Lists.newArrayList(this.__data)
        ).map(
            function (bsonDoc) {
                return JSON.parse(
                    bsonDoc.toJson()
                )
            }
        );
    }
};