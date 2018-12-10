package com.github.tsingjyujing.lofka.nightwatcher.util

import org.bson.Document

import scala.collection.JavaConverters._

/**
  * Document工具类
  */
object DocumentUtil {

    /**
      * 使用类似Map的语法生成Document
      *
      * @param elements key->value
      * @return
      */
    def Doc(elements: (String, Any)*): Document = new Document(
        Map(elements.map(u => (u._1, u._2.asInstanceOf[AnyRef])): _*).asJava
    )
}
