package com.github.tsingjyujing.lofka.nightwatcher

import java.util.Properties

import com.github.tsingjyujing.lofka.nightwatcher.basic.IRichService
import com.github.tsingjyujing.lofka.nightwatcher.service.{CommonProcessor, DynamicService, HeartbeatWriter, NginxProcessor}
import com.github.tsingjyujing.lofka.nightwatcher.util.JsonDocumentSchema
import com.github.tsingjyujing.lofka.util.FileUtil
import com.google.common.collect.Lists
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09
import org.bson.Document
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ArrayBuffer

/**
  * 流计算入口
  */
object StreamingEntry {
    protected val LOGGER: Logger = LoggerFactory.getLogger("Lofka Streaming Entry")

    /**
      * 入口地址
      *
      * @param args
      */
    def main(args: Array[String]): Unit = {

        // 动态脚本的位置

        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        val params = ParameterTool.fromArgs(args)
        env.getConfig.setGlobalJobParameters(
            params
        )

        // 日志数据源
        val logSource: DataStream[Document] = env.addSource({
            val properties: Properties = FileUtil.autoReadProperties("lofka-kafka-client.properties")
            val topicList: java.util.List[String] = Lists.newArrayList(
                properties.getProperty("kafka.topic", "logger-json").split(","): _*
            )
            new FlinkKafkaConsumer09[Document](topicList, new JsonDocumentSchema(), properties)
        })

        val services: ArrayBuffer[IRichService[Document]] = ArrayBuffer[IRichService[Document]](
            new CommonProcessor(),
            new NginxProcessor(),
            new HeartbeatWriter()
        )

        try{
            services += new DynamicService(
                params.get("dynamics")
            )
            LOGGER.info("DynamicService initialized.")
         }catch {
            case _:Throwable=>
        }
        services.foreach(
            _.richStreamProcessing(env, logSource)
        )

        env.execute("LofkaStreaming")
    }
}
