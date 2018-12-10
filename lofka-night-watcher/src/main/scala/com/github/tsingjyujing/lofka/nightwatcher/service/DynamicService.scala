package com.github.tsingjyujing.lofka.nightwatcher.service

import com.github.tsingjyujing.lofka.nightwatcher.basic.IRichService
import com.github.tsingjyujing.lofka.nightwatcher.engine.EngineProxy
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.bson.Document

/**
  * 动态服务
  *
  * @param baseConfigureUrl 基础配置JSON文件的URL
  */
class DynamicService(baseConfigureUrl: String) extends IRichService[Document] {
    /**
      * 处理带有初始化信息和配置的流
      *
      * @param env       环境信息
      * @param configure 配置文件
      */
    override def richStreamProcessing(env: StreamExecutionEnvironment, ds: DataStream[Document]): Unit = {

        ds.addSink(
            new RichSinkFunction[Document] {

                val engine: EngineProxy = new EngineProxy(
                    baseConfigureUrl
                )

                override def invoke(value: Document, context: SinkFunction.Context[_]): Unit = {
                    engine.executeAll(
                        value.toJson()
                    )
                }
            }
        ).name("Dynamics Processing")
    }
}
