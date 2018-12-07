package com.github.tsingjyujing.lofka.nightwatcher.service

import com.github.tsingjyujing.lofka.nightwatcher.basic.IRichService
import com.github.tsingjyujing.lofka.nightwatcher.sink.BaseMongoDBSink
import com.github.tsingjyujing.lofka.nightwatcher.util.DocumentUtil
import com.github.tsingjyujing.lofka.nightwatcher.util.DocumentUtil.Doc
import com.github.tsingjyujing.lofka.util.FileUtil
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.FindOneAndUpdateOptions
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.bson.Document

class HeartbeatWriter extends IRichService[Document] {
    /**
      * 处理带有初始化信息和配置的流
      *
      * @param env       环境信息
      * @param configure 配置文件
      */
    override def richStreamProcessing(env: StreamExecutionEnvironment, ds: DataStream[Document]): Unit = {
        val heartBeatSource: DataStream[Document] = ds.filter(doc => try {
            doc.get("message", classOf[Document]).getString("type").equals("heartbeat")
        } catch {
            case _: Throwable => false
        }).flatMap(doc => try {
            val messageDocument = doc.get("message", classOf[Document])
            Some(
                Doc(
                    "app_name" -> doc.getString("app_name"),
                    "heartbeat_name" -> messageDocument.getString("name"),
                    "interval" -> messageDocument.getDouble("interval"),
                    "receive_tick" -> System.currentTimeMillis().toDouble
                )
            )
        } catch {
            case _: Throwable => None
        })

        // 心跳包更新写入MongoDB
        heartBeatSource.addSink(
            new BaseMongoDBSink[Document](
                FileUtil.autoReadProperties("lofka-statistics-mongo.properties"),
                "logger", "heartbeat"
            ) {


                /**
                  * 数据库的准备阶段
                  *
                  * @param collection
                  */
                override def prepare(collection: MongoCollection[Document]): Unit = {
                    collection.createIndex(DocumentUtil.Doc(
                        "app_name" -> 1,
                        "heartbeat_name" -> 1
                    ))
                }

                override def invoke(value: Document, context: SinkFunction.Context[_]): Unit = {
                    coll.findOneAndUpdate(
                        DocumentUtil.Doc(
                            "app_name" -> value.getString("app_name"),
                            "heartbeat_name" -> value.getString("heartbeat_name")
                        ),
                        DocumentUtil.Doc(
                            "$set" -> value
                        ),
                        new FindOneAndUpdateOptions()
                            .upsert(true)
                    )
                }
            }
        ).name("heartbeat-update")
    }
}

