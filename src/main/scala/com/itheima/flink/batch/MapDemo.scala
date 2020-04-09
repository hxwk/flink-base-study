package com.itheima.flink.batch

import org.apache.flink.api.scala._

object MapDemo {

  case class User(id: String, name: String)

  def main(args: Array[String]): Unit = {
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    val dataSet = env.fromCollection(List(
      "1,张三", "2,李四", "3,王五"
    ))
    val result: DataSet[User] = dataSet.map{
      str => {
        val arrs: Array[String] = str.split(",")
        User(arrs(0), arrs(1))
      }
    }
    result.print()
  }
}
