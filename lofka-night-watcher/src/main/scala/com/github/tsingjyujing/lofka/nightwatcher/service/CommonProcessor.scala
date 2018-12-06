package com.github.tsingjyujing.lofka.nightwatcher.service

import com.github.tsingjyujing.lofka.nightwatcher.basic.{Keyable, KeyableStatisticable}
import com.github.tsingjyujing.lofka.nightwatcher.keys.CommonKey
import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.bson.Document

class CommonProcessor extends KeyableProcessor("common") {
    override def convertStream(ds: DataStream[Document]): DataStream[KeyableStatisticable[Keyable[String]]] = ds.flatMap(
        doc => try {
            val key = CommonKey(
                doc.getString("app_name"),
                doc.getString("level").toUpperCase,
                try {
                    doc.getString("logger")
                } catch {
                    case _: Throwable => null
                },
                doc.get("host", classOf[Document]).getString("ip")
            )

            Some(KeyableStatisticable.createFromNumbers[Keyable[String]](
                key,
                Map(
                    "count" -> 1
                )
            ))
        } catch {
            case _: Throwable => None
        }
    )
}