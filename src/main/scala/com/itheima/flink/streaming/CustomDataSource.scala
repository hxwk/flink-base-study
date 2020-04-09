package com.itheima.flink.streaming

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.itheima.flink.streaming.CustomDataSource.Order
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._

import scala.util.Random

object CustomDataSource extends App {
  /**
   * 1.  创建订单样例类
   * 2.  获取流处理环境
   * 3.  创建自定义数据源
   *     - 循环1000次
   *     - 随机构建订单信息
   *     - 上下文收集数据
   *     - 每隔一秒执行一次循环
   * 4.  打印数据
   * 5.  执行任务
   */
    //1.  创建订单样例类
    case class Order(id:String,userid:Int,money:Int,eventTime:Long)
    //2.  获取流处理环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //3.  创建自定义数据源
    val orderDataStream: DataStream[Order] = env.addSource(new CustomDataSource)
    //   *     - 循环1000次
    //   *     - 随机构建订单信息
    //   *     - 上下文收集数据
    //   *     - 每隔一秒执行一次循环

    //4.  打印数据
    orderDataStream.print()
    //5. 执行任务
    env.execute(this.getClass.getName)
  }

  class CustomDataSource extends SourceFunction[Order]{
    var isRunning =true
    override def run(ctx: SourceFunction.SourceContext[Order]): Unit = {
      for(i<-0 until 1000 if isRunning){
        val id = UUID.randomUUID().toString
        var userid = Random.nextInt(3)
        val money = Random.nextInt(101)
        val eventTime = System.currentTimeMillis()
        ctx.collect(Order(id,userid,money,eventTime))
        TimeUnit.SECONDS.sleep(1)
      }
    }

    override def cancel(): Unit = {
      isRunning = false
    }

}
