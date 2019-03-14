package com.github.tsingjyujing.lofka.nightwatcher

import java.util.Properties

import com.github.tsingjyujing.lofka.nightwatcher.basic.IRichService
import com.github.tsingjyujing.lofka.nightwatcher.service._
import com.github.tsingjyujing.lofka.nightwatcher.util.JsonDocumentSchema
import com.github.tsingjyujing.lofka.util.FileUtil
import com.google.common.collect.Lists
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
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
      * @param args dynamics 动态脚本地址
      *             parallelism 默认并发
      */
    def main(args: Array[String]): Unit = {

        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        val params = ParameterTool.fromArgs(args)
        env.getConfig.setGlobalJobParameters(
            params
        )

        val kafkaConsumer: FlinkKafkaConsumer011[Document] = {
            val properties: Properties = try {
                FileUtil.autoReadProperties("lofka-kafka-client.properties", getClass)
            } catch {
                case ex: Throwable =>
                    LOGGER.error("Error while reading kafka client setting.", ex)
                    null
            }
            val topicList: java.util.List[String] = Lists.newArrayList(
                properties.getProperty("kafka.topic", "logger-json").split(","): _*
            )
            val consumer = new FlinkKafkaConsumer011[Document](topicList, new JsonDocumentSchema(), properties)
            consumer.setStartFromGroupOffsets()
            consumer.setCommitOffsetsOnCheckpoints(true)
            consumer
        }

        env.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
        env.setParallelism(params.get("parallelism", "1").toInt)

        // 日志数据源
        val logSource: DataStream[Document] = env.addSource(kafkaConsumer)

        /**
          * 这里定义所有监听的服务（动态服务除外）
          */
        val services: ArrayBuffer[IRichService[Document]] = ArrayBuffer[IRichService[Document]](
            new CommonProcessor(),
            new NginxProcessor(),
            new HeartbeatWriter(),
            new PersistenceData(),
            new ErrorAggregate(
                compareRatio = 0.6,
                ttlMillsSeconds = 60000,
                maxAggregateCount = 500
            )
        )

        /**
          * 尝试启动动态计算服务
          */
        if (params.has("dynamics")) {
            try {
                services += new DynamicService(
                    params.get("dynamics")
                )
                LOGGER.info("DynamicService initialized.")
            } catch {
                case ex: Throwable =>
                    LOGGER.warn("Initialize dynamic service failed which caused by:", ex)
            }
        }

        /**
          * 逐一启动服务
          */
        services.foreach(
            _.richStreamProcessing(env, logSource)
        )


        env.execute("LofkaStreaming")
    }
}
