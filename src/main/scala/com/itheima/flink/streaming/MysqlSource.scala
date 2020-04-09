package com.itheima.flink.streaming

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala._

object MysqlSource {

  /**
   * 1. 自定义Source,继承自RichSourceFunction
   * 2. 实现run方法
   *    1. 加载驱动
   *    2. 创建连接
   *    3. 创建PreparedStatement
   *    4. 执行查询
   *    5. 遍历查询结果,收集数据
   * 3. 使用自定义Source
   * 4. 打印结果
   * 5. 执行任务
   */
  case class User(id: Int, username: String, password: String, name: String)

  def main(args: Array[String]): Unit = {
    //流处理环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val userDataStream: DataStream[User] = env.addSource(new CustomMysqlSource)
    userDataStream.print
    env.execute(this.getClass.getName)
  }

  class CustomMysqlSource extends RichSourceFunction[User] {
    var conn: Connection = _

    override def open(parameters: Configuration): Unit = {
      //   *    1. 加载驱动
      Class.forName("com.mysql.jdbc.Driver")
      //   *    2. 创建连接
      val url = "jdbc:mysql://192.168.139.160:3306/test?characterEncoding=utf-8"
      val username = "root"
      val password = "123456"
      conn = DriverManager.getConnection(url, username, password)
    }

    override def close(): Unit = {
      if (!conn.isClosed) {
        conn.close()
      }
    }

    override def run(ctx: SourceFunction.SourceContext[User]): Unit = {
      //   *    3. 创建PreparedStatement
      val sql = "select id,username,password,name from users"
      val statement: PreparedStatement = conn.prepareStatement(sql)
      //   *    4. 执行查询
      val rs: ResultSet = statement.executeQuery()
      //   *    5. 遍历查询结果,收集数据
      while (rs.next()) {
        val id = rs.getInt("id")
        val username = rs.getString("username")
        val password = rs.getString("password")
        val name = rs.getString("name")
        ctx.collect(User(id, username, password, name))
      }
    }

    override def cancel(): Unit = ???
  }

}
