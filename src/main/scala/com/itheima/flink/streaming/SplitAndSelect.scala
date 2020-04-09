package com.itheima.flink.streaming

import org.apache.flink.streaming.api.scala._

object SplitAndSelect {
  def main(args: Array[String]): Unit = {
    // 1. 创建批处理环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 2. 设置并行度
    env.setParallelism(1)
    // 3. 加载本地集合
    val elements: DataStream[Int] = env.fromElements(1, 2, 3, 4, 5, 6)
    // 4. 数据分流,分为奇数和偶数
    val split_data: SplitStream[Int] = elements.split(
      (num: Int) =>
        num % 2 match {
          case 0 => List("even")
          case 1 => List("odd")
        }
    )
    // 5. 获取分流后的数据
    val even: DataStream[Int] = split_data.select("even")
    val odd: DataStream[Int] = split_data.select("odd")
    val all: DataStream[Int] = split_data.select("odd", "even")

    // 6. 打印数据
    odd.print()

    // 7. 执行任务
    env.execute()
  }
}