package com.itheima.flink.cachefile

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

import scala.collection.JavaConverters._
import scala.collection.mutable

object CacheFileDemo extends App {
  /**
   * 1.创建批处理环境
   * 2.注册本地文件
   * 3.map操作
   * 4.打印输出
   */
  private val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  env.registerCachedFile("D:\\workspace\\flink-base-project13\\data\\data.txt","data")
  private val dataSet: DataSet[String] = env.fromCollection(List(
    "spark", "flink", "tez", "druid", "phoenix"
  ))
  private val result: DataSet[String] = dataSet.map(new RichMapFunction[String, String] {

    var files: mutable.Buffer[String] = _

    override def open(parameters: Configuration): Unit = {
      //1.注册的参数 2.广播变量 3. 分布式缓存 4.累加器 5.环境参数 通过getRuntimeContext
      val data: File = getRuntimeContext.getDistributedCache.getFile("data")
      files = FileUtils.readLines(data).asScala
    }

    override def map(value: String): String = {
      value+" on "+ files(0)
    }
  })

  //打印结果
  result.print()
}
