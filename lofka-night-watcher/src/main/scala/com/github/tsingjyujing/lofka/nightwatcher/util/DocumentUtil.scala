package com.github.tsingjyujing.lofka.nightwatcher.util

import org.bson.Document

import scala.collection.JavaConverters._

object DocumentUtil {

    def Doc(elements: (String, Any)*): Document = new Document(
        Map(elements.map(u => (u._1, u._2.asInstanceOf[AnyRef])): _*).asJava
    )
}
