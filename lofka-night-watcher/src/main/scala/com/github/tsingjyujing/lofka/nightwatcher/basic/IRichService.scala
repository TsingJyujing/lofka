package com.github.tsingjyujing.lofka.nightwatcher.basic

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

/**
  * 带有配置信息的服务
  *
  * @tparam T 数据类型
  */
trait IRichService[T] extends Serializable {

    /**
      * 处理带有初始化信息和配置的流
      *
      * @param env 环境信息
      * @param ds  数据流
      */
    def richStreamProcessing(
                                env: StreamExecutionEnvironment,
                                ds: DataStream[T]
                            ): Unit
}