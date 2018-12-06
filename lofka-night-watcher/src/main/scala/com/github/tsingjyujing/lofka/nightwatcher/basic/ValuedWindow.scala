package com.github.tsingjyujing.lofka.nightwatcher.basic

case class ValuedWindow[V](value: V, windowStart: Long, windowEnd: Long) extends Valuable[V] {
    /**
      * Get value
      *
      * @return
      */
    override def getValue: V = value
}
