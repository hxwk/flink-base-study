package com.itheima.flink.batch

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.api.scala._

object BatchFromCollection {
  /**
   * 1.创建批处理执行换
   * 2.设置并行度
   * 3.加载本地集合文件
   * 4.打印本地集合
   */
  def main(args: Array[String]): Unit = {
    //1.创建批处理执行换
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //2.设置并行度
    env.setParallelism(1)
    //3.加载本地集合文件  spark  sc.parallel  sc.makeRDD
    //val dataSet: DataSet[Int] = env.fromElements(1, 2, 3, 4, 5)
//    val dataSet: DataSet[String] = env.fromCollection(Seq("hadoop", "spark", "flink"))
    /*val dataSet: DataSet[(String, String)] = env.fromCollection(Map(
      "1" -> "zhangsan",
      "2" -> "lisi",
      "3" -> "wangwu"
    ))*/
    //val dataSet: DataSet[String] = env.fromCollection(Set("hadoop", "hadoop", "hadoop", "flink", "spark", "spark", "spark", "spark", "flink"))
    val dataSet: DataSet[Long] = env.generateSequence(1, 10)
    //4.打印本地集合
    dataSet.print()
  }
}
