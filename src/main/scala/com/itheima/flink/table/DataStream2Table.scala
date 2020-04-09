package com.itheima.flink.table

import org.apache.flink.core.fs.FileSystem
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.{Table, TableEnvironment}
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.sinks.CsvTableSink

object DataStream2Table {
  /**
   * 1.  获取流处理环境
   * 2.  获取TableEnvironment
   * 3.  加载本地集合
   * 4.  根据数据注册表
   * 5.  执行SQL
   * 6.  写入CSV文件中
   * 7.  执行任务
   */
  case class Order1(user: Long, product: String, amount: Int)

  def main(args: Array[String]): Unit = {
    //1.  获取流处理环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //2.  获取TableEnvironment
    val tableEnv: StreamTableEnvironment = TableEnvironment.getTableEnvironment(env)
    //3.  加载本地集合
    val orderDataStream: DataStream[Order1] = env.fromCollection(List(
      Order1(1L, "beer", 3),
      Order1(1L, "diaper", 4),
      Order1(3L, "rubber", 2)))
    //4.  根据数据注册表
    tableEnv.registerDataStream("t_order",orderDataStream)
    //5.  执行SQL
    val table: Table = tableEnv.sqlQuery("select * from t_order")
    //6.  写入CSV文件中
    table.writeToSink(new CsvTableSink("./data/order.csv",",",1,FileSystem.WriteMode.OVERWRITE))
    //7.  执行任务
    env.execute(this.getClass.getName)
  }
}
