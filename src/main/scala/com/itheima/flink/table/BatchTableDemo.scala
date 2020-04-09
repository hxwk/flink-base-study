package com.itheima.flink.table

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.table.api.{Table, TableEnvironment, Types}
import org.apache.flink.table.sinks.CsvTableSink
import org.apache.flink.table.sources.CsvTableSource

object BatchTableDemo {
  def main(args: Array[String]): Unit = {
    //创建batch执行环境
    val env = ExecutionEnvironment.getExecutionEnvironment
    //创建table环境用于batch查询
    val tableEnvironment = TableEnvironment.getTableEnvironment(env)
    //加载外部CSV数据 通过CsvTableSource创建表的数据源，读csv文件
    val csvTableSource:CsvTableSource = CsvTableSource.builder()
      .path("./data/score.csv") //文件路径
      .field("id", Types.INT) //第一列数据
      .field("name", Types.STRING) //第二列数据
      .field("subjectId", Types.INT) //第三列数据
      .field("score", Types.DOUBLE) //第四列数据
      .fieldDelimiter(",") //列分隔符，默认是"，"
      .lineDelimiter("\n") //换行符
      .ignoreFirstLine() //忽略第一行
      .ignoreParseErrors() //忽略解析错误
      .build()
    //将外部数据构建成表 ，通过表数据源注册为tableA表
    tableEnvironment.registerTableSource("tableA", csvTableSource)

    //TODO 1：使用table方式查询数据 Table API算子查询数据
    val table: Table = tableEnvironment.scan("tableA")
      .select("id , name , subjectId,score")
      .filter("name == '张三'")
    table.printSchema()
    //将数据写出去 ，使用CsvTableSink 将数据写入到csv文件中。
    table.writeToSink(new CsvTableSink("./data/table.csv", ",", 1, FileSystem.WriteMode.OVERWRITE))
    //TODO 2：使用sql方式
    //    val sqlResult = tableEnvironment.sqlQuery("select id,name,subjectId,score from tableA where name='张三'")
    env.execute()
  }
}
