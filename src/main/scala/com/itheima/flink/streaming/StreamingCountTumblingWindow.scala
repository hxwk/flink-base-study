package com.itheima.flink.streaming

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object StreamingCountTumblingWindow {
  def main(args: Array[String]): Unit = {
    //1.创建运行环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //2.定义数据流来源
    val textStream = env.socketTextStream("node01", 9999)
    //3.转换数据格式，text->CountCart
    val data = textStream.map(line => {
      val array = line.split(",")
      CountCart(array(0).toInt, array(1).toInt)
    })
    //4.执行统计操作，每个sensorId一个tumbling窗口，窗口的大小为5秒
    //按照key进行收集，对应的key出现的次数达到5次作为一个结果
    val keyByData: KeyedStream[CountCart, Int] = data.keyBy(line => line.sen)
    //相同的key出现三次才做一次sum聚合
    val result = keyByData.countWindow(3).sum(1)
    //5、显示统计结果
    result.print()
    //6、触发流计算
    env.execute()
  }
}
case class CountCart(sen:Int, cardNum:Int)
