package com.itheima.flink.batch

import org.apache.flink.api.scala._


object JoinDemo {
  /**
   * 1. 分别将资料中的两个文件复制到项目中的`data/join/input`中
   * 2. 构建批处理环境
   * 3. 创建两个样例类
   * * 学科Subject（学科ID、学科名字）
   * * 成绩Score（唯一ID、学生姓名、学科ID、分数——Double类型）
   * 4. 分别使用`readCsvFile`加载csv数据源，并制定泛型
   * 5. 使用join连接两个DataSet，并使用`where`、`equalTo`方法设置关联条件
   * 6. 打印关联后的数据源
   */
  def main(args: Array[String]): Unit = {
    case class Score(id:Int,name:String,subid:Int,score:Double)
    case class Subject(subid:Int,subName:String)

    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //score
    val scoreDataSet: DataSet[Score] = env.readCsvFile[Score]("./data/score.csv")
    //subject
    val subjectDataSet: DataSet[Subject] = env.readCsvFile[Subject]("./data/subject.csv")
    //join
    val result: DataSet[(Int, String, String, Double)] = scoreDataSet
      .join(subjectDataSet)
      .where(_.subid)
      .equalTo(_.subid)
      .apply((score, subject) => {
      (score.id, score.name, subject.subName, score.score)
    })
    //打印结果
    result.print()
  }
}
