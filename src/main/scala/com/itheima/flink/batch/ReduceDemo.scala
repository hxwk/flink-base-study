package com.itheima.flink.batch

import org.apache.flink.api.java.aggregation.Aggregations
import org.apache.flink.api.scala._

object ReduceDemo {
  def main(args: Array[String]): Unit = {
    //创建流处理环境
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //设置并行度
    env.setParallelism(1)
    //加载本地数据集  reduceByKey
    val dataSet: DataSet[(String, Int)] = env.fromCollection(List(
        ("java", 1), ("java", 1), ("java", 2), ("scala", 1)
    ))
    //进行groupBy，reduce操作
    //val result: DataSet[(String, Int)] = dataSet.groupBy(0).reduce((pre, next) => (pre._1, pre._2 + next._2))
    val result = dataSet.groupBy(0).aggregate(Aggregations.MAX,1)
    //打印数据
    result.print()
  }
}
