package com.itheima.flink.table

import org.apache.flink.api.scala._
import org.apache.flink.table.api.{Table, TableEnvironment}

//todo 确定是不是导包问题
object Table2DataSet {
  /**
   * 1. 获取流处理环境
   * 2. 设置并行度
   * 3. 获取Table运行环境
   * 4. 加载本地集合
   * 5. 转换DataSet为Table
   * 6. 将table转换为DataSet----将一个表附加到流上Append Mode
   * 7. 将table转换为DataSet----Retract Mode true代表添加消息，false代表撤销消息
   * 8. 打印输出
   * 9. 执行任务
   */
  def main(args: Array[String]): Unit = {
    //1. 批处理环境
    val env = ExecutionEnvironment.getExecutionEnvironment
    //2. 设置并行度
    env.setParallelism(1)
    //3. 获取table运行环境
    val tabelEnv = TableEnvironment.getTableEnvironment(env)
    //4. 加载本地集合
    val collection: DataSet[(Long, Int, String)] = env.fromCollection(List(
      (1L, 1, "Hello"),
      (2L, 2, "Hello"),
      (3L, 3, "Hello"),
      (7L, 7, "Hello World"),
      (8L, 8, "Hello World"),
      (20L, 20, "Hello World")))
    //5. DataSet转换为Table
    import org.apache.flink.table.api.scala._
    val table: Table = tabelEnv.fromDataSet(collection,'id,'userid,'name)
    table.printSchema()
    //6. table转换为dataSet
    val toDataSet: DataSet[(Long, Int, String)] = tabelEnv.toDataSet[(Long, Int, String)](table)
    //7.打印数据
    toDataSet.print()
    //    env.execute()
  }
}
