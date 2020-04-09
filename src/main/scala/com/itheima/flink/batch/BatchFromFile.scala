package com.itheima.flink.batch

import java.util.Properties

import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

object BatchFromFile {
  /**
   * 1.  读取本地文件数据
   * 2.  读取HDFS文件数据
   * 3.  读取CSV文件数据
   * 4.  读取压缩文件
   * 5.  遍历目录
   */
  def main(args: Array[String]): Unit = {
    case class Subject(id: Int, subName: String)
    //创建批处理运行环境
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //设置并行度
    env.setParallelism(1)
    //加载本地文件
    //val dataSet: DataSet[String] = env.readTextFile("./data/data.txt")
    //2.  读取HDFS文件数据
    //val dataSet: DataSet[String] = env.readTextFile("hdfs://node01:8020/tmp/c.txt")
    //3.  读取CSV文件数据

    //    val dataSet: DataSet[Subject] = env.readCsvFile[Subject]("./data/subject.csv")
    //val dataSet: DataSet[String] = env.readTextFile("D:\\workspace\\flink-base-project13\\data\\wordcount.txt.gz")
    //5.  遍历目录
    val params = new Configuration
    params.setBoolean("recursive.file.enumeration",true)
    val dataSet = env.readTextFile("./data").withParameters(params)
    //打印文件
    dataSet.print()
  }
}
