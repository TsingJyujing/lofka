package com.github.tsingjyujing.lofka.nightwatcher.trigger

import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.api.common.state.{ReducingState, ReducingStateDescriptor}
import org.apache.flink.api.common.typeutils.base.LongSerializer
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.windowing.triggers.Trigger.TriggerContext
import org.apache.flink.streaming.api.windowing.triggers._
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.slf4j.{Logger, LoggerFactory}

/**
  * 针对时间窗做处理，在超出窗体的时间或者个数极限的时候触发Fire&Purge
  */
class TimeCountTrigger[W <: TimeWindow](
                                           maxCount: Long,
                                           timeCharacteristic: TimeCharacteristic = TimeCharacteristic.ProcessingTime
                                       ) extends Trigger[Object, W] {

    protected val LOGGER: Logger = LoggerFactory.getLogger(getClass)

    /**
      * 计数状态
      */
    private val countState: ReducingStateDescriptor[java.lang.Long] = new ReducingStateDescriptor[java.lang.Long](
        "count", new Sum(), LongSerializer.INSTANCE
    )

    /**
      * 这里绝逼是Flink里的坑，在返回`TriggerResult.FIRE_AND_PURGE`的时候不会清空计数
      *
      * @param window 窗信息
      * @param ctx    上下文
      * @return
      */
    private def fireAndPurge(window: W, ctx: TriggerContext): TriggerResult = {
        clear(window, ctx)
        TriggerResult.FIRE_AND_PURGE
    }


    override def onElement(element: Object, timestamp: Long, window: W, ctx: TriggerContext): TriggerResult = {
        val count: ReducingState[java.lang.Long] = ctx.getPartitionedState(countState)
        count.add(1L)
        if (count.get >= maxCount || timestamp >= window.getEnd) {
            LOGGER.debug(
                "Triggered on element while count:{}/{} time:{}/{}.",
                count.get.toString,
                maxCount.toString,
                timestamp.toString,
                window.getEnd.toString
            )
            fireAndPurge(window, ctx)
        } else {
            TriggerResult.CONTINUE
        }
    }

    override def onProcessingTime(time: Long, window: W, ctx: TriggerContext): TriggerResult = {
        if (timeCharacteristic == TimeCharacteristic.EventTime) {
            TriggerResult.CONTINUE
        } else {
            if (time >= window.getEnd) {
                TriggerResult.CONTINUE
            } else {
                LOGGER.debug("Triggered on processing time.")
                fireAndPurge(window, ctx)
            }
        }
    }

    override def onEventTime(time: Long, window: W, ctx: TriggerContext): TriggerResult = {
        if (timeCharacteristic == TimeCharacteristic.ProcessingTime) {
            TriggerResult.CONTINUE
        } else {
            if (time >= window.getEnd) {
                TriggerResult.CONTINUE
            } else {
                LOGGER.debug("Triggered on event time.")
                fireAndPurge(window, ctx)
            }
        }
    }

    override def clear(window: W, ctx: TriggerContext): Unit = {
        val count: ReducingState[java.lang.Long] = ctx.getPartitionedState(countState)
        count.clear()
        count.add(0L)
    }

    /**
      * 计数求和
      */
    class Sum extends ReduceFunction[java.lang.Long] {
        def reduce(value1: java.lang.Long, value2: java.lang.Long): java.lang.Long = value1 + value2
    }

}
