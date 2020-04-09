package com.itheima.flink.batch

import org.apache.flink.api.scala._

object MinMaxDemo {
  def main(args: Array[String]): Unit = {
    //1. 构建批处理运行环境
    val environment: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //2. 使用fromCollection构建测试数据集
    val dataSet: DataSet[(Int, Double, String)] = environment.fromCollection(List(
      (1, 98.0, "语文"), (2, 90.5, "语文"), (3, 99.5, "数学"), (4, 95.5, "数学"), (3, 78.0, "语文")
      , (2, 91.5, "数学"), (1, 92.5, "数学")))
    //3. 使用minBy/maxBy对元组指定field进行取值排序
    val minDataSet: DataSet[(Int, Double, String)] = dataSet.minBy(1)
    //4. maxBy
    val maxDataSet: DataSet[(Int, Double, String)] = dataSet.groupBy(2).maxBy(1)
    //5. 打印输出元组数据
    minDataSet.print()
    println("=========================")
    maxDataSet.print()
  }
}
