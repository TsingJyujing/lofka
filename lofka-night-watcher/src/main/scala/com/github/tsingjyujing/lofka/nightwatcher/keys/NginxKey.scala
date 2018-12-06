package com.github.tsingjyujing.lofka.nightwatcher.keys

import com.github.tsingjyujing.lofka.nightwatcher.basic.Keyable
import com.github.tsingjyujing.lofka.nightwatcher.util.DocumentUtil
import org.bson.Document

/**
  * Nginx 主键信息
  *
  * @param appName      应用名称
  * @param ip           IP
  * @param upstreamAddr 内网机器地址
  * @param host         所连接的HOST信息
  * @param uri          URI
  */
case class NginxKey(
                       appName: String,
                       ip: String,
                       upstreamAddr: String,
                       host: String,
                       uri: String
                   ) extends Keyable[String] {

    override def getKey: String = s"$appName $ip $upstreamAddr $host $uri"

    override def toDocument: Document = DocumentUtil.Doc(
        "app_name" -> appName,
        "ip" -> ip,
        "upstream_addr" -> upstreamAddr,
        "host" -> host,
        "uri" -> uri
    )
}

object NginxKey {
    /**
      * 从Lofka的日志中生成主键
      *
      * @param lofkaLog lofka日志
      * @return
      */
    def apply(
                 lofkaLog: Document
             ): NginxKey = {
        val messageDocument = lofkaLog.get("message", classOf[Document])
        val hostDocument = lofkaLog.get("host", classOf[Document])
        NginxKey(
            lofkaLog.getString("app_name"),
            hostDocument.getString("ip"),
            messageDocument.getString("upstream_addr"),
            messageDocument.getString("host"),
            messageDocument.getString("uri")
        )
    }
}