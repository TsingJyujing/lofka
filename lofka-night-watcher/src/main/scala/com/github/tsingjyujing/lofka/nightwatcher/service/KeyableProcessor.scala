package com.github.tsingjyujing.lofka.nightwatcher.service

import com.github.tsingjyujing.lofka.nightwatcher.basic.{IRichService, Keyable, KeyableStatisticable, ValuedWindow}
import com.github.tsingjyujing.lofka.nightwatcher.sink.BaseMongoDBSink
import com.github.tsingjyujing.lofka.nightwatcher.util.DocumentUtil
import com.github.tsingjyujing.lofka.util.FileUtil
import com.mongodb.client.MongoCollection
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.function.{ProcessAllWindowFunction, ProcessWindowFunction}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.bson.Document

import scala.collection.JavaConverters._

/**
  * 针对 Keyable[String] 进行分类统计的
  *
  * @param collectionPrefix 使用的 Collection 的前缀
  */
abstract class KeyableProcessor(collectionPrefix: String) extends IRichService[Document] {

    implicit def timeToLong(time: Time): Long = time.toMilliseconds

    def convertStream(ds: DataStream[Document]): DataStream[KeyableStatisticable[Keyable[String]]]

    /**
      * 处理带有初始化信息和配置的流
      *
      * @param env 环境信息
      * @param ds  数据
      */
    override def richStreamProcessing(env: StreamExecutionEnvironment, ds: DataStream[Document]): Unit = {
        // Nginx 日志统计，按照1000ms进行了预聚合
        val aggr1sLog: DataStream[KeyableStatisticable[Keyable[String]]] = convertStream(ds).keyBy(
            _.getKey
        ).timeWindow(
            Time.seconds(1)
        ).process(
            new ProcessWindowFunction[
                KeyableStatisticable[Keyable[String]],
                KeyableStatisticable[Keyable[String]],
                String,
                TimeWindow
                ] {
                override def process(
                                        key: String,
                                        context: Context,
                                        elements: Iterable[KeyableStatisticable[Keyable[String]]],
                                        out: Collector[KeyableStatisticable[Keyable[String]]]
                                    ): Unit = {
                    out.collect(
                        elements.reduce(_ + _)
                    )
                }
            }
        )

        val statisticCollection: String = collectionPrefix + "_statistics"
        val realtimeCollection: String = collectionPrefix + "_realtime"
        val aggr5minLog: DataStream[KeyableStatisticable[Keyable[String]]] = tumblingAggregateAppender(
            aggr1sLog,
            Time.minutes(5),
            statisticCollection
        )

        val aggr1hourLog: DataStream[KeyableStatisticable[Keyable[String]]] = tumblingAggregateAppender(
            aggr5minLog,
            Time.hours(1),
            statisticCollection
        )

        /**
          * 数据插入更新
          */
        slidingAggregateRefresher(aggr1sLog, Time.hours(1), Time.minutes(1), realtimeCollection)
        slidingAggregateRefresher(aggr5minLog, Time.days(1), Time.hours(1), realtimeCollection)
        slidingAggregateRefresher(aggr1hourLog, Time.days(7), Time.hours(4), realtimeCollection)
        slidingAggregateRefresher(aggr1hourLog, Time.days(30), Time.hours(8), realtimeCollection)
    }


    /**
      * 处理滑动窗口的统计
      *
      * @param ds             数据源
      * @param aggregateTime  聚合时间
      * @param updateTime     更新周期
      * @param collectionName 存入集合名称
      */
    def slidingAggregateRefresher(
                                     ds: DataStream[KeyableStatisticable[Keyable[String]]],
                                     aggregateTime: Long,
                                     updateTime: Long,
                                     collectionName: String
                                 ): Unit = {
        ds.windowAll(
            SlidingProcessingTimeWindows.of(
                Time.milliseconds(aggregateTime),
                Time.milliseconds(updateTime)
            )
        ).process(
            new ProcessAllWindowFunction[KeyableStatisticable[Keyable[String]], Iterable[ValuedWindow[KeyableStatisticable[Keyable[String]]]], TimeWindow]() {
                override def process(context: Context, elements: Iterable[KeyableStatisticable[Keyable[String]]], out: Collector[Iterable[ValuedWindow[KeyableStatisticable[Keyable[String]]]]]): Unit = {
                    out.collect(
                        elements.groupBy(_.getKey).map(_._2.reduce(_ + _)).map(x => {
                            ValuedWindow(
                                x,
                                context.window.getStart,
                                context.window.getEnd
                            )
                        })
                    )
                }
            }
        ).addSink(
            new BaseMongoDBSink[Iterable[ValuedWindow[KeyableStatisticable[Keyable[String]]]]](
                FileUtil.autoReadProperties("lofka-statistics-mongo.properties", classOf[KeyableProcessor]),
                "logger", collectionName
            ) {
                override def invoke(value: Iterable[ValuedWindow[KeyableStatisticable[Keyable[String]]]], context: SinkFunction.Context[_]): Unit = {
                    coll.deleteMany(new Document("window_size", aggregateTime))
                    coll.insertMany(
                        value.map(x => {
                            val doc = x.value.key.toDocument
                            x.value.value.foreach(
                                kv => doc.append(kv._1, kv._2.toDocument)
                            )
                            doc.append("window_start", x.windowStart)
                            doc.append("window_end", x.windowEnd)
                            doc.append("window_size", aggregateTime)
                            doc
                        }).toIndexedSeq.asJava
                    )
                }
            }
        ).name(s"$collectionPrefix-$aggregateTime-sliding-statistics")

    }

    /**
      * 通过键值按周期分类统计，并且追加到数据库
      *
      * @param ds
      * @param aggregateTime  聚合时间
      * @param collectionName 写入Collection的名称
      * @return
      */
    def tumblingAggregateAppender(
                                     ds: DataStream[KeyableStatisticable[Keyable[String]]],
                                     aggregateTime: Long,
                                     collectionName: String
                                 ): DataStream[KeyableStatisticable[Keyable[String]]] = {
        val aggregatedStream: DataStream[ValuedWindow[KeyableStatisticable[Keyable[String]]]] = ds.keyBy(_.getKey).timeWindow(
            Time.milliseconds(aggregateTime)
        ).process(
            new ProcessWindowFunction[KeyableStatisticable[Keyable[String]], ValuedWindow[KeyableStatisticable[Keyable[String]]], String, TimeWindow] {
                override def process(
                                        key: String,
                                        context: Context,
                                        elements: Iterable[KeyableStatisticable[Keyable[String]]],
                                        out: Collector[ValuedWindow[KeyableStatisticable[Keyable[String]]]]
                                    ): Unit = {
                    out.collect(
                        ValuedWindow[KeyableStatisticable[Keyable[String]]](
                            elements.reduce(_ + _),
                            context.window.getStart,
                            context.window.getEnd
                        )
                    )
                }
            }
        )

        aggregatedStream.addSink(
            new BaseMongoDBSink[ValuedWindow[KeyableStatisticable[Keyable[String]]]](
                FileUtil.autoReadProperties("lofka-statistics-mongo.properties", classOf[KeyableProcessor]),
                "logger", collectionName
            ) {

                /**
                  * 数据库的准备阶段
                  *
                  * @param collection
                  */
                override def prepare(collection: MongoCollection[Document]): Unit = {
                    collection.createIndex(DocumentUtil.Doc(
                        "window_start" -> 1
                    ))

                    collection.createIndex(DocumentUtil.Doc(
                        "window_end" -> 1
                    ))

                    collection.createIndex(DocumentUtil.Doc(
                        "window_size" -> 1
                    ))

                    collection.createIndex(DocumentUtil.Doc(
                        "app_name" -> 1
                    ))
                }

                override def invoke(value: ValuedWindow[KeyableStatisticable[Keyable[String]]], context: SinkFunction.Context[_]): Unit = {
                    val staticalResult: KeyableStatisticable[Keyable[String]] = value.value
                    val doc = staticalResult.key.toDocument
                    staticalResult.value.foreach(
                        kv => doc.append(kv._1, kv._2.toDocument)
                    )
                    doc.append("window_start", value.windowStart)
                    doc.append("window_end", value.windowEnd)
                    doc.append("window_size", aggregateTime)
                    coll.insertOne(doc)
                }
            }
        ).name(s"$collectionPrefix-$aggregateTime-window-statistics")
        aggregatedStream.map(_.value)
    }

}
