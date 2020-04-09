package com.itheima.flink.batch

import org.apache.flink.api.scala._

object ReduceGroupDemo {
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
    val result: DataSet[(String, Int)] = dataSet.groupBy(0).reduceGroup(iter=>{
      iter.reduce((a,b)=>(a._1,a._2+b._2))
    })
    //打印数据
    result.print()
  }
}
