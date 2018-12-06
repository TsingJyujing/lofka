package com.github.tsingjyujing.lofka.nightwatcher.basic

import com.github.tsingjyujing.lofka.nightwatcher.util.SimpleNumberStatistics
import org.bson.Document

/**
  * 统计信息
  *
  * @param key   键
  * @param value 统计数值 内容
  * @tparam KT 键类型
  */
case class KeyableStatisticable[KT <: Keyable[String]](
                                                          key: KT,
                                                          value: Map[String, SimpleNumberStatistics]
                                                      ) extends Keyable[String] with Valuable[Map[String, SimpleNumberStatistics]] {
    /**
      * 获取Key
      *
      * @return
      */
    override def getKey: String = key.getKey

    /**
      * Get value
      *
      * @return
      */
    override def getValue: Map[String, SimpleNumberStatistics] = value

    /**
      * Merge two element
      *
      * @param v value to merge
      * @return
      */
    def +(v: KeyableStatisticable[KT]): KeyableStatisticable[KT] = {
        val s1: Set[String] = getValue.keySet
        val s2: Set[String] = v.getValue.keySet
        assert(getKey.equals(v.getKey), s"Can't merge caused by different key: $getKey != ${v.getKey}")
        assert(s1.size == s2.size && s1.subsetOf(s2), s"Can't merge caused different value set: ${s1.toString} != ${s2.toString}")
        KeyableStatisticable(
            key,
            s1.map(k => (k, getValue(k) + v.getValue(k))).toMap
        )
    }

    override def toDocument: Document = {
        val doc = key.toDocument
        value.foreach(
            kv=>doc.append(kv._1,kv._2.toDocument)
        )
        doc
    }
}

object KeyableStatisticable {
    /**
      * 从数值中获取数据
      *
      * @param key   键
      * @param value 数值信息
      * @tparam KT 主键类型
      * @return
      */
    def createFromNumbers[KT <: Keyable[String]](key: KT, value: Map[String, Double]): KeyableStatisticable[KT] = new KeyableStatisticable[KT](
        key,
        value.map(kv => (kv._1, SimpleNumberStatistics(kv._2)))
    )
}