package com.itheima.flink.batch

import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem

object SinkFileDemo {
  def main(args: Array[String]): Unit = {
    //Map(1 -> "spark", 2 -> "flink")
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val dataSet: DataSet[Map[Int, String]] = env.fromCollection(List(
      Map(1 -> "spark", 2 -> "flink")
    ))
    dataSet.writeAsText("hdfs://node01:8020/tmp/collect.txt",FileSystem.WriteMode.OVERWRITE)
    dataSet.print()
  }
}
