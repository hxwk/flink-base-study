package com.itheima.flink.batch

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._

object RebalanceDemo {
  /**
   * 1. 构建批处理运行环境
   * 2. 使用`env.generateSequence`创建0-100的并行数据
   * 3. 使用`fiter`过滤出来`大于`10的数
   * 4. 使用map操作传入`RichMapFunction`，将当前子任务的ID和数字构建成一个元组
   * 在RichMapFunction中可以使用`getRuntimeContext.getIndexOfThisSubtask`获取子任务序号
   * 5. 打印测试
   */
  def main(args: Array[String]): Unit = {
    //
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    //生成数据
    val dataSet: DataSet[Long] = env.generateSequence(0, 100)
    //filter
    val filterDataSet: DataSet[Long] = dataSet.filter(_ > 10).rebalance()
    //map
    val mapDataSet: DataSet[(Int, Long)] = filterDataSet.map(new RichMapFunction[Long, (Int, Long)] {
      override def map(value: Long): (Int, Long) = {
        (getRuntimeContext.getIndexOfThisSubtask, value)
      }
    })
    //print
    mapDataSet.print()
  }
}
