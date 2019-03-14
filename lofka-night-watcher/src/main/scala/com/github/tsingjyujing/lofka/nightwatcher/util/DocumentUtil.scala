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

    /**
      * Option 包装器
      *
      * @param data option数据
      * @tparam T 类型
      */
    case class OptionSelectWrapper[T](data: Option[T]) {

        /**
          * 存在的时候处理的逻辑
          *
          * @param func 处理函数
          * @return
          */
        def onDefined(func: T => Unit): OptionSelectWrapper[T] = {
            if (data.isDefined) {
                func(data.get)
            }
            this
        }

        /**
          * 不存在的时候的处理函数
          *
          * @param sub 处理函数
          * @return
          */
        def onUndefined(sub: () => Unit): OptionSelectWrapper[T] = {
            if (data.isEmpty) {
                sub()
            }
            this
        }
    }

    implicit def optionSelectProcessor[T](data: Option[T]): OptionSelectWrapper[T] = OptionSelectWrapper(data)
}
