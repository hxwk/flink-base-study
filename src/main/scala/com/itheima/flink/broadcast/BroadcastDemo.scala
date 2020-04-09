package com.itheima.flink.broadcast

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

import scala.collection.JavaConverters._
import scala.collection.mutable

object BroadcastDemo extends App {
  /*private val params: ParameterTool = ParameterTool.fromArgs(args)
  params.get("input")*/
  /**
   * 1. 获取批处理运行环境
   * 2. 分别创建两个数据集
   * 3. 使用`RichMapFunction`对`成绩`数据集进行map转换
   * 4. 在数据集调用`map`方法后，调用`withBroadcastSet`将`学生`数据集创建广播
   * 5. 实现`RichMapFunction`
   *    - 将成绩数据(学生ID，学科，成绩) -> (学生姓名，学科，成绩)
   *    - 重写`open`方法中，获取广播数据
   *    - 导入`scala.collection.JavaConverters._`隐式转换
   *    - 将广播数据使用`asScala`转换为Scala集合，再使用toList转换为scala `List`集合
   *    - 在`map`方法中使用广播进行转换
   * 6. 打印测试
   */
  //1. 获取批处理运行环境
  private val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  //2. 分别创建两个数据集
  private val scoreDataSet: DataSet[(Int, String, Int)] = env.fromCollection(List((1, "语文", 50), (2, "数学", 70), (3, "英文", 86)))
  private val studentDataSet: DataSet[(Int, String)] = env.fromCollection(List((1, "张三"), (2, "李四"), (3, "王五")))
  //3. 使用`RichMapFunction`对`成绩`数据集进行map转换
  private val result: DataSet[(String, String, Int)] = scoreDataSet.map(new RichMapFunction[(Int, String, Int), (String, String, Int)] {
    var student: mutable.Buffer[(Int, String)] = _

    override def open(parameters: Configuration): Unit = {
      student = getRuntimeContext.getBroadcastVariable[(Int, String)]("student").asScala
    }

    override def map(score: (Int, String, Int)): (String, String, Int) = {
      val matchStudent: mutable.Buffer[(Int, String)] = student
        .filter(stu => stu._1 == score._1)
      if (matchStudent != null && matchStudent.size > 0) {
        (matchStudent(0)._2, score._2, score._3)
      } else {
        ("", "", 0)
      }
    }
    override def close(): Unit = {}
  }).withBroadcastSet(studentDataSet, "student")
  //4. 在数据集调用`map`方法后，调用`withBroadcastSet`将`学生`数据集创建广播
  result.print()
}
