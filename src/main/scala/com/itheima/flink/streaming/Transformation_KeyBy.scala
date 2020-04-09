package com.itheima.flink.streaming

import org.apache.flink.streaming.api.scala._

object Transformation_KeyBy {
  def main(args: Array[String]): Unit = {
    // 1.获取流处理运行环境
    val senv = StreamExecutionEnvironment.getExecutionEnvironment
    // 2.设置并行度
    senv.setParallelism(3)
    //3. 获取Socket数据源
    val stream = senv.socketTextStream("node01", 9999, '\n')
    //4. 转换操作,以空格切分,每个元素计数1,以单词分组,累加
    val text = stream.flatMap(_.split("\\s"))
      .map((_,1))   //spark hadoop flink oozie
      //TODO 逻辑上将一个流分成不相交的分区，每个分区包含相同键的元素。
      // 在内部，这是通过散列分区来实现的
      .keyBy(0)  //keyedState  keyBy
      //TODO 这里的sum并不是分组去重后的累加值，如果统计去重后累加值，则使用窗口函数
      .sum(1)
    //5. 打印到控制台
    text.print()
    //6. 执行任务
    senv.execute()
  }
}
