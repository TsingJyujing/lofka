package com.github.tsingjyujing.lofka.nightwatcher.sink

import java.util.Properties

import com.github.tsingjyujing.lofka.persistence.util.sink.MongoDBConnectionLoader
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.bson.Document


/**
  * MongoDB 单条写入Sink，适用于数据量不大，实时性高的场景
  *
  * @param connectionConfigure 连接配置信息
  * @param db                  数据库名称
  * @param collection          数据存储地点
  * @param prepare             准备工作（例如建立索引，清空数据等等）
  */
abstract class BaseMongoDBSink[T](
                                     connectionConfigure: Properties,
                                     db: String,
                                     collection: String
                                 ) extends RichSinkFunction[T] {

    var conn: MongoClient = null
    var coll: MongoCollection[Document] = null


    /**
      * 数据库的准备阶段
      *
      * @param collection
      */
    def prepare(collection: MongoCollection[Document]): Unit = {}


    override def close(): Unit = {
        if (conn != null) {
            conn.close()
        }
    }

    override def open(parameters: Configuration): Unit = {
        if (coll == null || conn == null) {
            conn = MongoDBConnectionLoader.createConnectionFromProperties(connectionConfigure)
            coll = conn.getDatabase(db).getCollection(collection)
            prepare(coll)
        }
    }
}
