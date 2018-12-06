package com.github.tsingjyujing.lofka.nightwatcher.basic

import org.bson.Document

/**
  * 可生成用于分区的Key的
  */
trait Keyable[T] {

    /**
      * 获取Key
      *
      * @return
      */
    def getKey: T

    override def hashCode(): Int = getKey.hashCode()

    override def equals(obj: Any): Boolean = obj match {
        case k: Keyable[T] => k.getKey.equals(getKey)
        case _ => false
    }

    def toDocument:Document
}
