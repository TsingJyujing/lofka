package com.github.tsingjyujing.lofka.nightwatcher.keys

import com.github.tsingjyujing.lofka.nightwatcher.basic.Keyable
import com.github.tsingjyujing.lofka.nightwatcher.util.DocumentUtil
import org.bson.Document

/**
  * 一般的应用的统计
  *
  * @param appName 应用名称
  * @param level   等级
  * @param logger  日志的LoggerName
  * @param ip      IP
  */
case class CommonKey(
                        appName: String,
                        level: String,
                        logger: String,
                        ip: String
                    ) extends Keyable[String] {
    /**
      * 获取Key
      *
      * @return
      */
    override def getKey: String = s"$appName $level $logger $ip"

    override def toDocument: Document = DocumentUtil.Doc(
        "app_name" -> appName,
        "level" -> level,
        "logger" -> logger,
        "ip" -> ip
    )
}


object CommonKey {
    /**
      * 从Lofka的日志中生成主键
      *
      * @param lofkaLog lofka日志
      * @return
      */
    def apply(
                 lofkaLog: Document
             ): CommonKey = CommonKey(
        lofkaLog.getString("app_name"),
        lofkaLog.getString("level").toUpperCase,
        try {
            lofkaLog.getString("logger")
        } catch {
            case _: Throwable => "NULL"
        },
        lofkaLog.get("host", classOf[Document]).getString("ip")
    )
}