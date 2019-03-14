package com.github.tsingjyujing.lofka.nightwatcher.service


import com.github.tsingjyujing.lofka.nightwatcher.basic.IRichService
import com.github.tsingjyujing.lofka.nightwatcher.trigger.TimeCountTrigger
import com.github.tsingjyujing.lofka.nightwatcher.util.DocumentUtil.Doc
import com.github.tsingjyujing.lofka.persistence.basic.IBatchLoggerProcessor
import com.github.tsingjyujing.lofka.persistence.util.ConfigLoader.ProcessorInfo
import com.github.tsingjyujing.lofka.util.FileUtil
import com.google.gson.Gson
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.bson.Document
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

/**
  * 持久化数据
  */
class PersistenceData extends IRichService[Document] {
    /**
      * 处理带有初始化信息和配置的流
      *
      * @param env 环境信息
      * @param ds  数据流
      */
    override def richStreamProcessing(env: StreamExecutionEnvironment, ds: DataStream[Document]): Unit = {
        ds.timeWindowAll(Time.seconds(5)).trigger(
            new TimeCountTrigger(1000)
        ).process(
            new ProcessAllWindowFunction[Document, Map[String, Int], TimeWindow]() {

                lazy val GSON = new Gson()

                lazy val writers: Map[String, IBatchLoggerProcessor] = {
                    val processors = GSON.fromJson(
                        FileUtil.readTextResource("lofka-persistence-processors.json"),
                        classOf[java.util.Map[String, Any]]
                    ).asScala
                    processors.map(
                        kv => (kv._1, GSON.fromJson(GSON.toJson(kv._2), classOf[ProcessorInfo]).getProcessor)
                    ).toMap
                }

                override def process(context: Context, elements: Iterable[Document], out: Collector[Map[String, Int]]): Unit = {
                    val elementSize = elements.size
                    out.collect(
                        writers.map(
                            kv => (kv._1, try {
                                kv._2.processLoggers(elements.asJavaCollection)
                                elementSize
                            } catch {
                                case _: Throwable => 0
                            })
                        )
                    )
                }
            }
        ).addSink(
            new SinkFunction[Map[String, Int]] {
                override def invoke(value: Map[String, Int], context: SinkFunction.Context[_]): Unit = {
                    PersistenceData.LOGGER.debug(Doc(
                        "type" -> "persistence_statistics",
                        "data" -> Doc(value.toSeq: _*)
                    ).toJson())
                }
            }
        ).name("data-persistence-sink")
    }
}

object PersistenceData {
    private val LOGGER: Logger = LoggerFactory.getLogger(PersistenceData.getClass)
}
