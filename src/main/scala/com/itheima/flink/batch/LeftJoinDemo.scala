package com.itheima.flink.batch

import org.apache.flink.api.common.operators.base.JoinOperatorBase
import org.apache.flink.api.scala._

import scala.collection.mutable

object LeftJoinDemo {
  def main(args: Array[String]): Unit = {
    //1.创建批处理运行环境
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //2.加载左表，加载右表，加载到本地数据源
    val leftList: mutable.MutableList[(Int, String)] = mutable.MutableList((1, "张三"), (2, "李四"), (3, "王五"))
    val rightList: mutable.MutableList[(Int, String, Int)] = mutable.MutableList((1, "语文", 90), (2, "数学", 99), (3, "英语", 91))
    val leftTable: DataSet[(Int, String)] = env.fromCollection(leftList)
    val rightTable: DataSet[(Int, String, Int)] = env.fromCollection(rightList)
    //3.左外连接关联操作
    //BROADCAST_HASH_FIRST BROADCAST_HASH_SECOND 将小表进行广播
    //REPARTITION_HASH_FIRST REPARTITION_HASH_SECOND 将相对小一点的表进行重分区
    //REPARTITION_SORT_MERGE 使用排序合并的方式进行重分区
    val result: DataSet[(Int, String, String, Int)] = leftTable.leftOuterJoin(rightTable
      ,JoinOperatorBase.JoinHint.OPTIMIZER_CHOOSES)
      .where(0).equalTo(0)
      .apply((left, right) => {
        (left._1, left._2, right._2, right._3)
      })
    //4.打印输出
    result.print()
  }
}
