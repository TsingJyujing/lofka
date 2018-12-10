package com.github.tsingjyujing.lofka.nightwatcher.basic

/**
  * 绑定类型为V的信息的时间窗信息
  *
  * @param value       信息
  * @param windowStart 时间窗开始
  * @param windowEnd   时间窗结束
  * @tparam V 信息类型
  */
case class ValuedWindow[V](value: V, windowStart: Long, windowEnd: Long) extends Valuable[V] {
    /**
      * Get value
      *
      * @return
      */
    override def getValue: V = value
}
