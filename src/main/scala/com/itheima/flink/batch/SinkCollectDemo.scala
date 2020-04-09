package com.itheima.flink.batch

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.api.scala._

object SinkCollectDemo {
  def main(args: Array[String]): Unit = {
    /**
     * (19, "zhangsan", 178.8),
     * (17, "lisi", 168.8),
     * (18, "wangwu", 184.8),
     * (21, "zhaoliu", 164.8)
     *
     * 1.创建批处理环境
     * 2.设置并行度
     * 3.加载本地集合
     * 4.打印输出，同时打印出集合
     */
    //1.创建批处理环境
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //2.设置并行度
    env.setParallelism(1)
    //3.加载本地集合
    val dataSet: DataSet[(Int, String, Double)] = env.fromCollection(List(
      (19, "zhangsan", 178.8),
      (17, "lisi", 168.8),
      (18, "wangwu", 184.8),
      (21, "zhaoliu", 164.8)
    ))
    //4.打印输出，同时打印出集合
    dataSet.printToErr()
    println("=================")
    val tuples: Seq[(Int, String, Double)] = dataSet.collect()
    println(tuples)
  }
}
