package com.itheima.flink.batch

import org.apache.flink.api.scala._

object DistinctDemo {
  def main(args: Array[String]): Unit = {
    //1. 获取`ExecutionEnvironment`运行环境
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //2. 使用`fromCollection`构建数据源
    val dataSet: DataSet[(String, Int)] = env.fromCollection(List(
      ("java", 1), ("java", 2), ("scala", 1),("scala", 1)
    ))
    //3. 使用`distinct`指定按照哪个字段来进行去重
    val result: DataSet[(String, Int)] = dataSet.distinct(0,1)
    //4. 打印测试
    result.print()
  }
}
