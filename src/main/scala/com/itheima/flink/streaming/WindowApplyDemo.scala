package com.itheima.flink.streaming

import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object WindowApplyDemo extends App {
  /**
   * 1. 获取流处理运行环境
   * 2. 构建socket流数据源，并指定IP地址和端口号
   * 3. 对接收到的数据转换成单词元组
   * 4. 使用`keyBy`进行分流（分组）
   * 5. 使用`timeWinodw`指定窗口的长度（每3秒计算一次）
   * 6. 实现一个WindowFunction匿名内部类
   *    - 在apply方法中实现聚合计算
   *    - 使用Collector.collect收集数据
   * 7. 打印输出
   * 8. 启动执行
   * 9. 在Linux中，使用`nc -lk 端口号`监听端口，并发送单词
   */
  private val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  private val socketDataStream: DataStream[String] = env.socketTextStream("node01", 9999)
  private val mapDataStream: DataStream[(String, Int)] = socketDataStream
    .flatMap(_.split(",")
      .map(_ -> 1))
  private val keyedStream: KeyedStream[(String, Int), Tuple] = mapDataStream
    .keyBy(0)
  private val windowStream: WindowedStream[(String, Int), Tuple, TimeWindow] = keyedStream
    .timeWindow(Time.seconds(3))

  /*//将延迟5秒的数据保存到侧输出流
  private val latestWordcount = new OutputTag[(String, Int)]("latestWordcount")
  windowStream.allowedLateness(Time.seconds(5)).sideOutputLateData(latestWordcount)*/

  private val result: DataStream[(String, Int)] = windowStream
    .apply(new WindowFunction[(String, Int), (String, Int), Tuple, TimeWindow] {
    override def apply(key: Tuple, window: TimeWindow, input: Iterable[(String, Int)]
                       , out: Collector[(String, Int)]): Unit = {
      out.collect(input.reduce((pre, next) => (pre._1, pre._2 + next._2)))
    }
  })
  result.print
  env.execute(this.getClass.getName)
}
