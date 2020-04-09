package com.itheima.flink.streaming

import org.apache.flink.streaming.api.scala._

object SocketSourceDemo {
  /**
   * 1. 获取流处理运行环境
   * 2. 构建socket流数据源，并指定IP地址和端口号
   * 3. 对接收到的数据进行空格拆分
   * 4. 打印输出
   * 5. 启动执行
   * 6. 在Linux中，使用`nc -lk 端口号`监听端口，并发送单词
   */
  def main(args: Array[String]): Unit = {
    //1. 获取流处理运行环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    //2. 构建socket流数据源，并指定IP地址和端口号
    val dataStream: DataStream[String] = env.socketTextStream("node01", 9999)
    //3. 对接收到的数据进行空格拆分
    val result: DataStream[(String, Int)] = dataStream
      .flatMap(_.split("\\s+")
        .map(_ -> 1)).setParallelism(1)
      .keyBy(0)
      .sum(1)
    //4. 打印输出
    result.print()
    //5. 启动执行
    env.execute()
  }
}
