package com.itheima.flink.batch

import org.apache.flink.api.scala._

object LocalEnvDemo {
  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis()
    val env: ExecutionEnvironment = ExecutionEnvironment.createLocalEnvironment()
    val dataSet: DataSet[String] = env.fromCollection(Seq("hadoop", "spark", "flink"))
    dataSet.print()
    //5470
    println(System.currentTimeMillis() - startTime)
  }
}
