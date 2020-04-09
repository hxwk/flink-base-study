package com.itheima.flink.streaming

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

object StreamingTumblingTimeWindow {
  def main(args: Array[String]): Unit = {
    //1.创建运行环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //2.定义数据流来源
    val textStream = env.socketTextStream("node01", 9999)
    //3.转换数据格式，text->CarWc
    val data = textStream.map(line => {
      val array = line.split(",")
      WordCountCart(array(0).toInt, array(1).toInt)
    })
    //4.执行统计操作，每个sensorId一个tumbling窗口，窗口的大小为5秒
    //也就是说，每5秒钟统计一次，在这过去的5秒钟内，各个路口通过红绿灯汽车的数量。
    val keyByData: KeyedStream[WordCountCart, Int] = data.keyBy(line => line.sen)
    //无重叠数据，所以只需要给一个参数即可，每5秒钟统计一下各个路口通过红绿灯汽车的数量
    val result = keyByData
      .timeWindow(Time.seconds(5),Time.seconds(2))
      .sum(1)
    //5、显示统计结果
    result.print()
    //6、触发流计算
    env.execute()
  }
}

/**
 * @param sen     哪个红绿灯
 * @param cardNum 多少辆车
 */
case class WordCountCart(sen: Int, cardNum: Int)