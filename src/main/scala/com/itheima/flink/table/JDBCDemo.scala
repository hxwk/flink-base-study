package com.itheima.flink.table

import org.apache.flink.api.common.typeinfo.{BasicTypeInfo, TypeInformation}
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat.JDBCInputFormatBuilder
import org.apache.flink.api.java.typeutils.{RowTypeInfo, ValueTypeInfo}
import org.apache.flink.api.scala._
import org.apache.flink.types.Row

object JDBCDemo {
  def main(args: Array[String]): Unit = {
    val builder = new JDBCInputFormatBuilder

    val fieldTypes: Array[TypeInformation[_]] = Array(
      BasicTypeInfo.INT_TYPE_INFO,
      BasicTypeInfo.STRING_TYPE_INFO,
      BasicTypeInfo.STRING_TYPE_INFO,
      BasicTypeInfo.STRING_TYPE_INFO
    )

    val fieldNames = List("id", "username", "password", "name")

    val format: JDBCInputFormat = builder.setDrivername("com.mysql.jdbc.Driver")
      .setDBUrl("jdbc:mysql://192.168.139.160:3306/test?characterEncoding=UTF-8")
      .setFetchSize(2)
      .setUsername("root")
      .setPassword("123456")
      .setQuery("select * from users")
      .setResultSetConcurrency(1)
      .setRowTypeInfo(new RowTypeInfo(fieldTypes, fieldNames.toArray))
      .finish()

    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment

    /**
     *  需要输入实现InputFormat得实现类
     *  JDBCInputFormat为InputFormat得JDBC得实现，是一次性获取得批数据
     *  1.createInput 传入 JDBCInputFormat
     *  2.创建JDBCInputFormat
     *  3.设置RowTypeInfo
     *    3-1 将列得类型封装成Array
     *    3-2 将列得名字封装为数组
     */
    val dataSet: DataSet[Row] = env.createInput(format)
    dataSet.print()
  }
}
