package com.itheima.flink.aggragate

import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.FileSystem

object AggragateDemo extends App {
  /**
   * 1. 获取批处理环境
   * 2. 加载本地集合
   * 3. map转换
   *    1. 定义累加器
   *    2. 注册累加器
   *    3. 累加数据
   * 4. 数据写入到文件中
   * 5. 执行任务,获取任务执行结果对象(JobExecutionResult)
   * 6. 获取累加器数值
   * 7. 打印数值
   */
  private val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  private val dataSet: DataSet[String] = env.fromElements("a", "b", "c", "d")
  private val result: DataSet[String] = dataSet.map(new RichMapFunction[String, String] {
    var longCounter = new LongCounter

    override def open(parameters: Configuration): Unit = {
      getRuntimeContext.addAccumulator("count", longCounter)
    }

    override def map(value: String): String = {
      println(value)
      longCounter.add(1l)
      value
    }
  })

  //4. 数据写入到文件中
  result.writeAsText("./data/result.txt",FileSystem.WriteMode.OVERWRITE)
  //5. 执行任务,获取任务执行结果对象(JobExecutionResult)
  private val executionResult: JobExecutionResult = env.execute()
  //6. 获取累加器数值
  private val count: Long = executionResult.getAccumulatorResult[Long]("count")
  //7. 打印数值
  println(count)

}
