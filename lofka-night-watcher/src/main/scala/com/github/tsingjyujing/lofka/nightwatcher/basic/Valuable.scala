package com.github.tsingjyujing.lofka.nightwatcher.basic

/**
  * to pack a value with some type
  * you can make a collection with type _<:IValue[T] and getValue as T foreach
  *
  * @author tsingjyujing@163.com
  * @tparam T value type
  */
trait Valuable[+T] {

    /**
      * Get value
      *
      * @return
      */
    def getValue: T
}