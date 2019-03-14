package com.github.tsingjyujing.lofka.nightwatcher.service

import java.util
import java.util.concurrent.atomic.AtomicLong

import com.github.tsingjyujing.lofka.algorithm.cluster.bistring.LongestStringSubSequenceCalculator
import com.github.tsingjyujing.lofka.algorithm.cluster.common.{DivisibleGenerator, IDivisible, IndivisibleStringSet, SingleString}
import com.github.tsingjyujing.lofka.nightwatcher.basic.IRichService
import com.github.tsingjyujing.lofka.nightwatcher.sink.BaseMongoDBSink
import com.github.tsingjyujing.lofka.nightwatcher.util.DocumentUtil._
import com.github.tsingjyujing.lofka.util.FileUtil
import com.google.common.collect.Lists
import com.mongodb.client.MongoCollection
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.bson.Document

import scala.collection.JavaConverters._

/**
  * 错误消息聚合程序
  *
  * @param compareRatio      相似度临界值
  * @param ttlMillsSeconds   聚合会话超时时间（毫秒）
  * @param maxAggregateCount 最多聚合条数
  */
class ErrorAggregate(
                        compareRatio: Double = 0.6,
                        ttlMillsSeconds: Long = 60000,
                        maxAggregateCount: Int = 250
                    ) extends IRichService[Document] {
    /**
      * 处理带有初始化信息和配置的流
      *
      * @param env 环境信息
      * @param ds  数据流
      */
    override def richStreamProcessing(env: StreamExecutionEnvironment, ds: DataStream[Document]): Unit = {
        val alertStream: DataStream[Document] = ds.flatMap(doc => try {
            val level = doc.getString("level").toUpperCase()
            if (ErrorAggregate.levelSet.contains(level)) {
                Some(doc)
            } else {
                None
            }
        } catch {
            case _: Throwable => None
        })

        val stringMessageAlertStream = alertStream.filter(doc => try {
            doc.get("message").isInstanceOf[String]
        } catch {
            case _: Throwable => false
        })

        stringMessageAlertStream.addSink(new BaseMongoDBSink[Document](
            FileUtil.autoReadProperties("lofka-statistics-mongo.properties"),
            "logger", "aggregate_result"
        ) {
            /**
              * 数据库的准备阶段
              *
              * @param collection
              */
            override def prepare(collection: MongoCollection[Document]): Unit = {
                coll.createIndex(new Document("start_tick", 1))
                coll.createIndex(new Document("end_tick", 1))
                coll.createIndex(new Document("message_count", 1))
                coll.createIndex(new Document("first_message_length", 1))
                coll.createIndex(new Document("processed", "hashed"))
            }

            override def invoke(value: Document, context: SinkFunction.Context[_]): Unit = {
                val currentMessage = value.getString("message")
                val currentTick = value.getDouble("timestamp").toLong
                val satisfiedDataOption = coll.find(
                    Doc(
                        "end_tick" -> Doc(
                            "$gte" -> (currentTick - ttlMillsSeconds)
                        ),
                        "processed" -> false,
                        "message_count" -> Doc(
                            "$lte" -> maxAggregateCount
                        ),
                        // 提前筛选掉明显不符合条件的
                        "first_message_length" -> Doc(
                            "$gte" -> math.floor(currentMessage.length * compareRatio),
                            "$lte" -> math.ceil(currentMessage.length / compareRatio)
                        )
                    )
                ).projection(
                    Doc(
                        "_id" -> 1,
                        "first_message" -> 1
                    )
                ).asScala.find(k => {
                    val compareMessage = k.getString("first_message")
                    val seqSize = LongestStringSubSequenceCalculator.compute(
                        compareMessage,
                        currentMessage
                    )
                    val similarity = seqSize * 1.0 / math.max(compareMessage.length, currentMessage.length)
                    similarity > compareRatio
                })

                satisfiedDataOption.onDefined(satisfiedData => {
                    coll.updateOne(
                        Doc(
                            "_id" -> satisfiedData.getObjectId("_id")
                        ),
                        Doc(
                            "$push" -> Doc(
                                "documents" -> value
                            ),
                            "$inc" -> Doc(
                                "message_count" -> 1
                            ),
                            "$set" -> Doc(
                                "end_tick" -> currentTick
                            )
                        )
                    )
                }).onUndefined(() => {
                    coll.insertOne(Doc(
                        "first_message" -> currentMessage,
                        "first_message_length" -> currentMessage.length,
                        "start_tick" -> currentTick,
                        "end_tick" -> currentTick,
                        "processed" -> false,
                        "message_count" -> 1,
                        "documents" -> Lists.newArrayList(
                            value
                        )
                    ))
                })

                coll.find(Doc(
                    "end_tick" -> Doc(
                        // more wait 10s for unordered data
                        "$gte" -> (currentTick + ttlMillsSeconds + 10000)
                    ),
                    "processed" -> false
                )).asScala.foreach(doc => {
                    val docs = doc.get("documents", classOf[util.ArrayList[Document]]).asScala
                    val messagesGeneralize: util.List[IDivisible[String]] =
                        DivisibleGenerator.analysisCommonStrings(
                            1,
                            DivisibleGenerator.generateSequenceByComma(
                                docs.map(_.getString("message")).toArray
                            )
                        )
                    val loggerGeneralize =
                        DivisibleGenerator.analysisPrefixStrings(
                            1,
                            DivisibleGenerator.generateSequenceByComma(
                                docs.map(_.getString("logger")).toArray
                            )
                        )
                    val threadGeneralize =
                        DivisibleGenerator.analysisCommonStrings(
                            1,
                            DivisibleGenerator.generateSequenceByComma(
                                docs.map(_.getString("thread")).toArray
                            )
                        )

                    val appNameGeneralize = DivisibleGenerator.analysisPrefixStrings(
                        1,
                        DivisibleGenerator.generateSequenceByComma(
                            docs.map(_.getString("app_name")).toArray
                        )
                    )

                    coll.updateOne(
                        Doc(
                            "_id" -> doc.getObjectId("_id")
                        ),
                        Doc(
                            "$set" -> Doc(
                                "processed" -> true,
                                "generalize" -> Doc(
                                    "level_set" -> docs.map(_.getString("level")).toSet.toSeq.asJava,
                                    "app_name_set" -> docs.map(_.getString("app_name")).toSet.toSeq.asJava,
                                    "thread_set" -> docs.map(_.getString("thread")).toSet.toSeq.asJava,
                                    "logger_set" -> docs.map(_.getString("logger")).toSet.toSeq.asJava,
                                    "app_name_aggregate" -> ErrorAggregate.generateGeneralizeObject(
                                        appNameGeneralize.asScala
                                    ).toSeq.asJava,
                                    "thread_aggregate" -> ErrorAggregate.generateGeneralizeObject(
                                        threadGeneralize.asScala
                                    ).toSeq.asJava,
                                    "logger_aggregate" -> ErrorAggregate.generateGeneralizeObject(
                                        loggerGeneralize.asScala
                                    ).toSeq.asJava,
                                    "message_aggregate" -> ErrorAggregate.generateGeneralizeObject(
                                        messagesGeneralize.asScala
                                    ).toSeq.asJava
                                )
                            )
                        )
                    )
                })
            }
        })

    }
}

object ErrorAggregate {

    val levelSet: Set[String] = Set("WARN", "ERROR", "FATAL")

    def generateGeneralizeObject[T <: Comparable[T]](g: Iterable[IDivisible[T]]): Iterable[Document] = g map {
        case value: SingleString[T] =>
            Doc(
                "type" -> "String",
                "value" -> value.toString
            )
        case value: IndivisibleStringSet[T] =>
            Doc(
                "type" -> "StringSet",
                "value" -> value.getDataCopy.asScala.map(u => {
                    u.toString
                }).asJava
            )
        case value =>
            Doc(
                "type" -> "Unknown",
                "value" -> value.toString
            )
    }
}
