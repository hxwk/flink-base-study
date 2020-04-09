package com.itheima.flink.batch

import java.sql.{Connection, DriverManager}

import org.apache.flink.api.scala._

object RemoteSubmitDemo {
  def main(args: Array[String]): Unit = {
    /**
     * 1.创建远程执行环境
     * 2.加载本地数据源
     * 3.打印输出
     */
    val env: ExecutionEnvironment = ExecutionEnvironment.createRemoteEnvironment("node01"
      ,8081,
      "D:\\workspace\\flink-base-project13\\target\\flink-base-project13-1.0-SNAPSHOT.jar")

    val dataSet: DataSet[String] = env.fromCollection(Seq("hadoop", "spark", "flink"))
    dataSet.print()
    dataSet.collect().foreach(str=>{
      Class.forName("com.mysql.jdbc.Driver")
      val conn: Connection = DriverManager.getConnection("jdbc:mysql://node01:3306/dbname?CharactorEncodeing=utf-8")
      conn.prepareStatement("insert into t1 values(?,?,?,?)")



    })
  }
}
