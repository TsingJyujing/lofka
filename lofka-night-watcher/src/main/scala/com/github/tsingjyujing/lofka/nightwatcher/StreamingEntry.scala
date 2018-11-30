package com.github.tsingjyujing.lofka.nightwatcher

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.slf4j.{Logger, LoggerFactory}

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
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        env.execute("LofkaStreaming")
    }
}
