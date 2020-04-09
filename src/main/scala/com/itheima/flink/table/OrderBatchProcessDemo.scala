package com.itheima.flink.table

import org.apache.flink.api.scala._
import org.apache.flink.table.api.{Table, TableEnvironment}
import org.apache.flink.types.Row

object OrderBatchProcessDemo {

  /**
   * 1. 获取一个批处理运行环境
   * 2. 获取一个Table运行环境
   * 3. 创建一个样例类`Order`用来映射数据（订单id、用户名、订单日期、订单金额）
   * 4. 基于本地`Order`集合创建一个DataSet source
   * 5. 使用Table运行环境将DataSet注册为一张表
   * 6. 使用SQL语句来操作数据（统计用户消费订单的总金额、最大金额、最小金额、订单总数）
   * 7. 使用TableEnv.toDataSet将Table转换为DataSet
   * 8. 打印测试
   */
  //3. 创建一个样例类`Order`用来映射数据（订单id、用户名、订单日期、订单金额）
  //(1,"zhangsan","2018-10-20 15:30",358.5),
  case class Order(id: Long, username: String, eventTime: String, money: Double)

  def main(args: Array[String]): Unit = {
    //1. 获取一个批处理运行环境
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //2. 获取一个Table运行环境
    val tableEnv = TableEnvironment.getTableEnvironment(env)
    //4. 基于本地`Order`集合创建一个DataSet source
    val dataSet: DataSet[Order] = env.fromCollection(List(
      Order(1, "zhangsan", "2018-10-20 15:30", 358.5),
      Order(2, "zhangsan", "2018-10-20 16:30", 131.5),
      Order(3, "lisi", "2018-10-20 16:30", 127.5),
      Order(4, "lisi", "2018-10-20 16:30", 328.5),
      Order(5, "lisi", "2018-10-20 16:30", 432.5),
      Order(6, "zhaoliu", "2018-10-20 22:30", 451.0),
      Order(7, "zhaoliu", "2018-10-20 22:30", 362.0),
      Order(8, "zhaoliu", "2018-10-20 22:30", 364.0),
      Order(9, "zhaoliu", "2018-10-20 22:30", 341.0)
    ))
    //5. 使用Table运行环境将DataSet注册为一张表
    tableEnv.registerDataSet("t_order", dataSet)
    //6. 使用SQL语句来操作数据（统计用户消费订单的总金额、最大金额、最小金额、订单总数）
    val sql = "select username, sum(money),max(money),min(money),count(id) from t_order group by username"
    val orderTable: Table = tableEnv.sqlQuery(sql)
    //7. 使用TableEnv.toDataSet将Table转换为DataSet
    val result = tableEnv.toDataSet[Row](orderTable)
    //8. 打印测试
    result.print()
  }
}
