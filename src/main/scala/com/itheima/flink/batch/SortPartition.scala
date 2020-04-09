package com.itheima.flink.batch

import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem

object SortPartition {
  def main(args: Array[String]): Unit = {
    // 1. 获取`ExecutionEnvironment`运行环境
    val env = ExecutionEnvironment.getExecutionEnvironment
    // 1. 设置并行度为`2`
    env.setParallelism(2)
    // 2. 使用`fromCollection`构建测试数据集
    val numDataSet = env.fromCollection(List(1,1,1,1,1,1,1,2,2,2,2,2))
    // 3. 使用`partitionByHash`按照字符串的hash进行分区
    val partitionDataSet: DataSet[Int] = numDataSet.partitionByHash(_.toString)
    // 4. 调用`writeAsText`写入文件到`data/parition_output`目录中
    partitionDataSet.writeAsText("./data/parition_output",FileSystem.WriteMode.OVERWRITE)
    // 5. 打印测试
    partitionDataSet.print()
  }
}
