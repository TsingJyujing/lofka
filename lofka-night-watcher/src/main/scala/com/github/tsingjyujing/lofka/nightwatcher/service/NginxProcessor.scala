package com.github.tsingjyujing.lofka.nightwatcher.service

import com.github.tsingjyujing.lofka.nightwatcher.basic.{Keyable, KeyableStatisticable}
import com.github.tsingjyujing.lofka.nightwatcher.keys.NginxKey
import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.bson.Document

/**
  * Nginx 日志统计
  */
class NginxProcessor extends KeyableProcessor("nginx") {
    override def convertStream(ds: DataStream[Document]): DataStream[KeyableStatisticable[Keyable[String]]] = ds.flatMap(
        doc => try {
            assert(doc.getString("type").toLowerCase().equals("nginx"), "type not nginx")
            val messageDocument = doc.get("message", classOf[Document])
            val hostDocument = doc.get("host", classOf[Document])
            val upstreamTime = messageDocument.getDouble("upstream_response_time")

            val key = NginxKey(
                doc.getString("app_name"),
                hostDocument.getString("ip"),
                messageDocument.getString("upstream_addr"),
                messageDocument.getString("host"),
                messageDocument.getString("uri")
            )

            Some(KeyableStatisticable.createFromNumbers[Keyable[String]](
                key, Map(
                    "count" -> 1,
                    "upstream_time" -> messageDocument.getDouble("upstream_response_time"),
                    "body_size" -> messageDocument.getDouble("body_bytes_sent"),
                    "request_size" -> messageDocument.getDouble("request_length")
                )
            ))
        } catch {
            case _: Throwable => None
        }
    )
}