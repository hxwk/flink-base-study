package com.itheima.flink.batch

import org.apache.flink.api.scala._

object LocalCollectionEnvDemo {
  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis()
    val env: ExecutionEnvironment = ExecutionEnvironment.createCollectionsEnvironment
    val dataSet: DataSet[String] = env.fromCollection(Seq("hadoop", "spark", "flink"))
    dataSet.print()
    //317
    println(System.currentTimeMillis() - startTime)
  }
}
