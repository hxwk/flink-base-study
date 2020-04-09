package com.itheima.flink.state

import java.util
import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment, WindowedStream}
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * 1. 自定义样例类(id: Long, name: String, info: String, count: Int)
 * 2. 自定义数据源,继承RichSourceFunction
 * 3. 实现run方法, 每秒钟向流中注入10000个样例类
 */
case class Order(id: Long, name: String, info: String, count: Int)
class MyCustmoDataSource extends RichSourceFunction[Order]{
  var isRunning = true
  override def run(ctx: SourceFunction.SourceContext[Order]): Unit = {
    for(i<-0 until 10000 if isRunning){
      ctx.collect(Order(1L,"str_"+i,"info",1))
      TimeUnit.SECONDS.sleep(1)
    }
  }

  override def cancel(): Unit = isRunning = false
}

/**
 *  1. 继承Serializable
 *  2. 为总数count提供set和get方法
 */
class UDFState extends Serializable{
  private var count:Long = _
  def setState(s:Long) = count = s
  def getState = count
}

/**
 *    	1. 继承WindowFunction
 *    	2. 重写apply方法,对窗口数据进行总数累加
 *    	3. 继承ListCheckpointed
 *    	4. 重写snapshotState,制作自定义快照
 *    	5. 重写restoreState,恢复自定义快照
 */
class MyWindowAndChk extends WindowFunction[Order,Long,Tuple,TimeWindow] with ListCheckpointed[UDFState]{
  var total:Long = _
  override def apply(key: Tuple, window: TimeWindow, input: Iterable[Order], out: Collector[Long]): Unit = {
    var count:Long = 0
      for(order<-input){
        count= count + 1
      }
    out.collect(count)
    total = total+count
  }

  //生成快照信息，将当前的结果状态保存到state中
  override def snapshotState(checkpointId: Long, timestamp: Long): util.List[UDFState] = {
    val state = new UDFState
    val states = new util.ArrayList[UDFState]
    state.setState(total)
    states.add(state)
    states
  }

  //恢复快照,将当前状态里的值进行赋值
  override def restoreState(state: util.List[UDFState]): Unit = {
    //状态里存储是当前的最新的状态
    total = state.get(0).getState
  }
}

object CheckpointDemo {
  /**
   * 1. 创建流处理环境
   * 2. 开启checkpoint,间隔时间为6s
   * 3. 设置checkpoint位置
   * 4. 设置处理时间为事件时间
   * 5. 添加数据源
   * 6. 添加水印支持
   * 7. keyby分组
   * 8. 设置滑动窗口1s ,窗口时间为4s
   * 9. 指定自定义窗口
   * 10. 打印结果
   * 11. 执行任务
   */
  def main(args: Array[String]): Unit = {
    //1. 创建流处理环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //2. 开启checkpoint,间隔时间为6s 每6s创建一个checkpoint
    env.enableCheckpointing(6000)
    //3. 设置checkpoint位置
    env.setStateBackend(new FsStateBackend("hdfs://node01:8020/flink-checkpoints/"))
    //checkpoint间隔时间 每两个checkpoint之间要有最少多长时间的间隔
    env.getCheckpointConfig.setCheckpointInterval(2000)
    //做checkpoint超时时间
    env.getCheckpointConfig.setCheckpointTimeout(60000)
    //当前checkpoint任务失败了是不是终止当前任务，false 不是
    env.getCheckpointConfig.setFailOnCheckpointingErrors(false)
    //最大的checkpoint的并发
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(2)
    //当前的程序如果被取消，保留 checkpoint ，之后需要手动删除checkpoint
    env.getCheckpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    //重启机制  固定的重启策略，每10秒钟重启一次，重启5次。
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(5,10000))
    //4. 设置处理时间为事件时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //5. 添加数据源
    val orderDataStream: DataStream[Order] = env.addSource(new MyCustmoDataSource)
    //6. 添加水印支持
    val watermarkDataStream: DataStream[Order] = orderDataStream.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[Order](Time.seconds(2)) {
      override def extractTimestamp(element: Order): Long = System.currentTimeMillis()
    })
    //7. keyby分组
    val keyedStream: KeyedStream[Order, Tuple] = watermarkDataStream.keyBy("id")
    //8. 设置滑动窗口1s ,窗口时间为4s
    val windowedStream: WindowedStream[Order, Tuple, TimeWindow] = keyedStream.timeWindow(Time.seconds(4), Time.seconds(1))
    //9. 指定自定义窗口
    val result: DataStream[Long] = windowedStream.apply(new MyWindowAndChk)
    //10. 打印结果
    result.print()
    //11. 执行任务
    env.execute(this.getClass.getName)
  }
}

