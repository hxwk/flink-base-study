package com.itheima.flink.streaming

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.apache.commons.lang3.time.FastDateFormat
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

import scala.util.Random

object WaterMarkDemo {

  /**
   * 1.  创建流处理运行环境
   * 2.  设置处理时间为`EventTime`
   * 3.  创建一个订单样例类`Order`，包含四个字段（订单ID、用户ID、订单金额、时间戳）
   * 4.  创建一个自定义数据源
   *     -   随机生成订单ID（UUID）
   *     -   随机生成用户ID（0-2）
   *     -   随机生成订单金额（0-100）
   *     -   时间戳为当前系统时间
   *     -   每隔1秒生成一个订单
   *     -   添加水印
   *     -   允许延迟2秒
   *     -   在获取水印方法中，打印水印时间、事件时间和当前系统时间
   * 5.  按照用户进行分流
   * 6.  设置5秒的时间窗口
   * 7.  进行聚合计算
   * 8.  打印结果数据
   * 9.  启动执行流处理
   */
  //1.  创建订单样例类
  case class Order(id: String, userid: Int, money: Int, eventTime: Long)

  def main(args: Array[String]): Unit = {

    //2.  获取流处理环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //时间属性，用eventtime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //3.  创建自定义数据源
    val orderDataStream: DataStream[Order] = env.addSource(new CustomDataSources)
    //   *     - 循环1000次
    //   *     - 随机构建订单信息
    //   *     - 上下文收集数据
    //   *     - 每隔一秒执行一次循环
    val watermarkDataStream: DataStream[Order] = orderDataStream.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks[Order] {
      var currentTime: Long = _
      val maxOutofDelay = 2000L

      override def getCurrentWatermark: Watermark = {
        val watermark = new Watermark(currentTime - maxOutofDelay)
        //在获取水印方法中，打印水印时间、事件时间和当前系统时间
        val format: FastDateFormat = FastDateFormat.getInstance("yyyyMMdd HH:mm:ss")
        println(s"当前时间:${format.format(currentTime)},水印时间:${format.format(watermark.getTimestamp)}," +
          s"系统时间:${format.format(System.currentTimeMillis())}")
        watermark
      }

      override def extractTimestamp(element: Order, previousElementTimestamp: Long): Long = {
        currentTime = Math.max(element.eventTime, previousElementTimestamp)
        currentTime
      }
    })
    //按照用户进行分流
    val keyedStream: KeyedStream[Order, Int] = watermarkDataStream.keyBy(_.userid)
    //设置5秒的时间窗口
    val windowedStream: WindowedStream[Order, Int, TimeWindow] = keyedStream.timeWindow(Time.seconds(5))
    //进行聚合计算
    val result: DataStream[Order] = windowedStream.reduce((pre, next) => Order(pre.id, pre.userid, pre.money + next.money, 0L))
    //4.  打印数据
    result.print()
    //5. 执行任务
    env.execute(this.getClass.getName)
  }

  class CustomDataSources extends SourceFunction[Order] {
    var isRunning = true

    override def run(ctx: SourceFunction.SourceContext[Order]): Unit = {
      for (i <- 0 until 1000 if isRunning) {
        val id = UUID.randomUUID().toString
        var userid = Random.nextInt(3)
        val money = Random.nextInt(101)
        val eventTime = System.currentTimeMillis()
        ctx.collect(Order(id, userid, money, eventTime))
        TimeUnit.SECONDS.sleep(1)
      }
    }

    override def cancel(): Unit = {
      isRunning = false
    }
  }

}
