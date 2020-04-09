package com.itheima.flink.streaming

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object DataSourceCsvDemo {
  /**
   * 1.创建流处理执行环境
   * 2.加载hdfs csv文件
   * 3.打印输出
   * 4.任务执行
   */
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val dataStream: DataStream[String] = env.readTextFile("hdfs://node01:8020/tmp/score2.csv")
    dataStream.print("成绩文件")
    env.execute("读取hdfs的成绩文件的任务")
  }
}
