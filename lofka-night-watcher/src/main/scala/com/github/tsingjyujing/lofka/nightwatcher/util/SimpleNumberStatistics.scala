/*
 * Copyright 1993-2018 TsingJyujing
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tsingjyujing.lofka.nightwatcher.util

import org.bson.Document

/**
  * 数字简单统计
  *
  * @param minValue        最小值
  * @param maxValue        最大值
  * @param sumValue        求和
  * @param sumSquaredValue 平方后数值的和
  * @param count           计数
  */
case class SimpleNumberStatistics(minValue: Double, maxValue: Double, sumValue: Double, sumSquaredValue: Double, count: Long) {

    /**
      * 和另一个统计结果合并
      *
      * @param v 另一个统计结果
      * @return
      */
    def +(v: SimpleNumberStatistics): SimpleNumberStatistics = new SimpleNumberStatistics(
        math.min(v.minValue, minValue),
        math.max(v.maxValue, maxValue),
        v.sumValue + sumValue,
        v.sumSquaredValue + sumSquaredValue,
        v.count + count
    )

    /**
      * 流式状态更新方式获取方差
      *
      * @return
      */
    def variance: Double = if (count > 1) {
        (sumSquaredValue - average * average * count) / (count - 1)
    } else {
        Double.NaN
    }

    /**
      * 获取标准差
      *
      * @return
      */
    def std: Double = math.sqrt(variance)

    /**
      * 和另一个数字合并
      *
      * @param v 数字
      * @return
      */
    def +(v: Double): SimpleNumberStatistics = SimpleNumberStatistics.apply(v) + this

    /**
      * 求平均值
      *
      * @return
      */
    def average: Double = sumValue * 1.0 / count

    def toDocument: Document = DocumentUtil.Doc(
        "min" -> minValue,
        "max" -> maxValue,
        "mean" -> average,
        "sum" -> sumValue,
        "count" -> count,
        "var" -> variance
    )
}

object SimpleNumberStatistics {
    /**
      * 通过数字创建
      *
      * @param number 数字
      * @return
      */
    def apply(number: Double): SimpleNumberStatistics = new SimpleNumberStatistics(
        number,
        number,
        number,
        number * number,
        1L
    )

    /**
      * 通过数组创建
      *
      * @param numbers 数组
      * @return
      */
    def apply(numbers: Iterable[Double]): SimpleNumberStatistics = new SimpleNumberStatistics(
        numbers.min,
        numbers.max,
        numbers.sum,
        numbers.map(x => x * x).sum,
        numbers.size
    )
}
