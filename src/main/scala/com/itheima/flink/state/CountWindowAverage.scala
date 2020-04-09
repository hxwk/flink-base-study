package com.itheima.flink.state

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.util.Collector

/**
 * 1.通过状态管理的valuestate数据结构保存当前的中间结果状态，用于下一步的flatmap计算
 * 2.通过中间结果状态来计算平均 (currentSum._1 + 1, currentSum._2 + input._2)
 * 满足currentSum._1>=2 (input._1, newSum._2 / newSum._1)输出
 */
class CountWindowAverage extends RichFlatMapFunction[(Long, Long), (Long, Long)] {
  //(1L, 3L),
  //(1L, 5L),
  //(1L, 7L),
  //(1L, 4L),
  //(1L, 2L)

  //定义全局的值状态
  private var sum: ValueState[(Long, Long)] = _

  override def flatMap(input: (Long, Long), out: Collector[(Long, Long)]): Unit = {

    // access the state value  获取当前的状态值 （0,0）
    val tmpCurrentSum = sum.value()

    // If it hasn't been used before, it will be null
    //如果当前的状态值存在了就用当前状态值，如果不存在就用（0,0）
    val currentSum = if (tmpCurrentSum != null) {
      tmpCurrentSum
    } else {
      (0L, 0L)
    }

    // update the count   (1L, 3L)  (1L, 5L) （2,8）
    //将拿到的状态值和输入的值进行计算
    val newSum = (currentSum._1 + 1, currentSum._2 + input._2)

    // update the state  （0,0）
    sum.update(newSum)

    // if the count reaches 2, emit the average and clear the state  (1L, 7L) (1L, 4L) （2,11）
    if (newSum._1 >= 2) {
      out.collect((input._1, newSum._2 / newSum._1))   //（1,4） （1,5）
      //清空结果状态
      sum.clear()
    }
  }

  //先执行的open操作，获取当前的单值状态数据
  override def open(parameters: Configuration): Unit = {
    //get 累加器，广播变量，分布式缓存文件，参数
    //get 当前记录的状态
    sum = getRuntimeContext.getState(
      new ValueStateDescriptor[(Long, Long)]("average", createTypeInformation[(Long, Long)])
    )
  }
}


object ExampleCountWindowAverage extends App {
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  env.fromCollection(List(
    (1L, 3L),
    (1L, 5L),
    (1L, 7L),
    (1L, 4L),
    (1L, 2L)
  )).keyBy(_._1)
    .flatMap(new CountWindowAverage())
    .print()
  // the printed output will be (1,4) and (1,5)

  env.execute("ExampleManagedState")
}