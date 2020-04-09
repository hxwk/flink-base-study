package com.itheima.flink.streaming

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala._

object SinkMysqlDemo {
  /**
   * 1. 创建流执行环境
   * 2. 准备数据
   * 3. 添加sink
   *    - 构建自定义Sink,继承自`RichSinkFunction`
   *    - 重写`open`方法,获取`Connection`和`PreparedStatement`
   *    - 重写`invoke`方法,执行插入操作，将参数值传递到statement中
   *    - 重写`close`方法,关闭连接操作
   * 4. 执行任务
   */
  def main(args: Array[String]): Unit = {
    //1. 创建流执行环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //2. 准备数据
    val dataStream: DataStream[(Int, String, String, String)] = env.fromCollection(List(
      (12, "dazhuang", "123456", "大壮"),
      (13, "erya", "123456", "二丫"),
      (14, "sanpang", "123456", "三胖")
    ))
    //3. 添加sink
    dataStream.addSink(new RichSinkFunction[(Int, String, String, String)] {
      var conn:Connection = _
      var ps: PreparedStatement = _
      override def open(parameters: Configuration): Unit = {
        //- 重写`open`方法,获取`Connection`和`PreparedStatement`
        Class.forName("com.mysql.jdbc.Driver")
        val url = "jdbc:mysql://192.168.139.160:3306/test?characterEncoding=utf-8"
        val username = "root"
        val password="123456"
        conn = DriverManager.getConnection(url,username,password)
        //- 重写`invoke`方法,执行插入操作，将参数值传递到statement中
        val sql = "insert into users(id,username,password,name) values(?,?,?,?);"
        ps = conn.prepareStatement(sql)
      }

      //- 重写`close`方法,关闭连接操作
      override def close(): Unit = {
        if(!ps.isClosed){
          ps.close()
        }
        if(!conn.isClosed){
          conn.close()
        }
      }

      override def invoke(value: (Int, String, String, String), context: SinkFunction.Context[_]): Unit = {
        try {
          ps.setInt(1, value._1)
          ps.setString(2, value._2)
          ps.setString(3, value._3)
          ps.setString(4, value._4)
          ps.executeUpdate()
        } catch {
          case ex:Exception =>
            ex.printStackTrace()
        }
      }
    })
    //执行任务
    env.execute(this.getClass.getName)
  }
}
