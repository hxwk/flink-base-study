package com.itheima.flink.streaming

import java.util.concurrent.TimeUnit

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.streaming.api.scala._

object StreamingDemoConnectScala {
  def main(args: Array[String]): Unit = {
    // 1. 创建流式处理环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 2. 添加两个自定义数据源
    val text1: DataStream[Long] = env.addSource(new MyLongSourceScala)
    val text2: DataStream[String] = env.addSource(new MyStringSourceScala)
    // 3. 使用connect合并两个数据流,创建ConnectedStreams对象
    val connectedStreams: ConnectedStreams[Long, String] = text1.connect(text2)
    // 4. 遍历ConnectedStreams对象,转换为DataStream
    val result: DataStream[Any] = connectedStreams.map(line1 => {
      line1
    }, line2 => {
      line2
    })
    // 5. 打印输出,设置并行度为1
    result.print().setParallelism(1)
    // 6. 执行任务
    env.execute("StreamingDemoWithMyNoParallelSourceScala")
  }

  /**
   * 创建自定义并行度为1的source
   * 实现从1开始产生递增数字
   */
  class MyLongSourceScala extends SourceFunction[Long] {
    var count = 1L
    var isRunning = true

    override def run(ctx: SourceContext[Long]) = {
      while (isRunning) {
        ctx.collect(count)
        count += 1
        TimeUnit.SECONDS.sleep(1)
      }
    }

    override def cancel() = {
      isRunning = false
    }
  }

  /**
   * 创建自定义并行度为1的source
   * 实现从1开始产生递增字符串
   */
  class MyStringSourceScala extends SourceFunction[String] {
    var count = 1L
    var isRunning = true

    override def run(ctx: SourceContext[String]) = {
      while (isRunning) {
        ctx.collect("str_" + count)
        count += 1
        TimeUnit.SECONDS.sleep(1)
      }
    }

    override def cancel() = {
      isRunning = false
    }
  }
}
