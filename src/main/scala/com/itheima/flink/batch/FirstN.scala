package com.itheima.flink.batch

import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.scala._

import scala.collection.mutable
import scala.util.Random

object FirstN {
  def main(args: Array[String]): Unit = {
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    val stus: mutable.MutableList[(String, String, Int)] = mutable.MutableList(
      ("张三", "语文", 90),
      ("张三", "数学", 91),
      ("张三", "英语", 85),
      ("李四", "语文", 94),
      ("李四", "数学", 92),
      ("李四", "英语", 90),
      ("王五", "语文", 89),
      ("王五", "数学", 78),
      ("王五", "英语", 87)
    )
    val dataSet: DataSet[(String, String, Int)] = env.fromCollection(Random.shuffle(stus))
    //打散
    dataSet.print()
    //first n
    val result: DataSet[(String, String, Int)] = dataSet.groupBy(0)
      .sortGroup(2, Order.DESCENDING)
      .first(1)
    //打印
    result.print()
  }
}
