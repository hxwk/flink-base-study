package com.itheima.flink.streaming

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object SideOutputDemo extends App {
  /**
   * 1. 创建流处理环境
   * 2. 设置并行度为1
   * 3. 加载本地集合
   * 4. 创建outputTag，process操作实现sideoutPut，分为奇数和偶数
   * 5. 打印主分流后的数据，通过getSideOutput 获取分流数据
   * 6. 执行任务
   */
  private val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  env.setParallelism(1)
  private val dataStream: DataStream[Int] = env.fromCollection(List(1, 2, 3, 4, 5, 6))
  //奇数放到侧输出流
  private val oddOutputTag = new OutputTag[Int]("odd")
  private val result: DataStream[Int] = dataStream.process(new ProcessFunction[Int, Int] {
    override def processElement(value: Int, ctx: ProcessFunction[Int, Int]#Context,
                                out: Collector[Int]): Unit = {
      value % 2 match {
        case 0 => out.collect(value)
        case 1 => ctx.output(oddOutputTag, value)
      }
    }
  })
  //打印主分流后的数据获取偶数
  result.print("偶数")
  //result.getSideOutput(oddOutputTag).print("奇数")
  //将奇数继续分流拆分出来合数和质数
  //result.process()
  result.getSideOutput(oddOutputTag).process(new ProcessFunction[Int,Int]{
    override def processElement(value: Int, ctx: ProcessFunction[Int, Int]#Context, out: Collector[Int]): Unit = {

    }
  })
  env.execute(this.getClass.getName)
}
