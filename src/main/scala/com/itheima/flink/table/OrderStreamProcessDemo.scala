package com.itheima.flink.table

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.api.{Table, TableEnvironment}
import org.apache.flink.types.Row

import scala.util.Random

object OrderStreamProcessDemo {

  /**
   * 1. 获取流处理运行环境
   * 2. 获取Table运行环境
   * 3. 设置处理时间为`EventTime`
   * 4. 创建一个订单样例类`Order`，包含四个字段（订单ID、用户ID、订单金额、时间戳）
   * 5. 创建一个自定义数据源
   *    - 使用for循环生成1000个订单
   *    - 随机生成订单ID（UUID）
   *    - 随机生成用户ID（0-2）
   *    - 随机生成订单金额（0-100）
   *    - 时间戳为当前系统时间
   *    - 每隔1秒生成一个订单
   * 6. 添加水印，允许延迟2秒
   * 7. 导入`import org.apache.flink.table.api.scala._`隐式参数
   * 8. 使用`registerDataStream`注册表，并分别指定字段，还要指定rowtime字段
   * 9. 编写SQL语句统计用户订单总数、最大金额、最小金额
   *    - 分组时要使用`tumble(时间列, interval '窗口时间' second)`来创建窗口
   * 10. 使用`tableEnv.sqlQuery`执行sql语句
   * 11. 将SQL的执行结果转换成DataStream再打印出来
   * 12. 启动流处理程序
   */
  //4. 创建一个订单样例类`Order`，包含四个字段（订单ID、用户ID、订单金额、时间戳）
  case class Order(id: String, userid: Int, money: Int, eventTime: Long)

  def main(args: Array[String]): Unit = {
    //1. 获取流处理运行环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //2. 获取Table运行环境
    val tableEnv: StreamTableEnvironment = TableEnvironment.getTableEnvironment(env)
    //3. 设置处理时间为`EventTime`
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //
    //5. 创建一个自定义数据源
    val orderDataStream: DataStream[Order] = env.addSource(new RichSourceFunction[Order] {
      var isRunning = true

      override def run(ctx: SourceFunction.SourceContext[Order]): Unit = {
        //   - 使用for循环生成1000个订单
        //
        //   - 随机生成订单ID（UUID）
        //   - 随机生成用户ID（0-2）
        //   - 随机生成订单金额（0-100）
        //   - 时间戳为当前系统时间
        //   - 每隔1秒生成一个订单
        //
        for (i <- 0 until 1000 if isRunning) {
          val id = UUID.randomUUID().toString
          val userid = Random.nextInt(3)
          val money = Random.nextInt(101)
          val eventTime = System.currentTimeMillis()
          ctx.collect(Order(id, userid, money, eventTime))
          TimeUnit.SECONDS.sleep(1)
        }
      }

      override def cancel(): Unit = isRunning = false
    })

    //6. 添加水印，允许延迟2秒
    val watermarkDataStream: DataStream[Order] = orderDataStream
      .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[Order](Time.seconds(2)) {
        override def extractTimestamp(element: Order): Long = {
          element.eventTime
        }
      })
    //7. 导入`import org.apache.flink.table.api.scala._`隐式参数
    import org.apache.flink.table.api.scala._
    //8. 使用`registerDataStream`注册表，并分别指定字段，还要指定rowtime字段
    tableEnv.registerDataStream("t_order", watermarkDataStream, 'id, 'userid, 'money, 'eventTime.rowtime)
    //9. 编写SQL语句统计用户订单总数、最大金额、最小金额
    val sql =
      """
        |select
        |userid,
        |count(id) as totalOrder,
        |max(money) as maxMoney,
        |min(money) as minMoney
        |from t_order
        |group by
        |userid,
        |tumble(eventTime, interval '2' second)
        |""".stripMargin
    // tumble 滚动窗口
    // hop 滑动窗口
    // session 会话窗口
    //   - 分组时要使用`tumble(时间列, interval '窗口时间' second)`来创建窗口
    //
    //10. 使用`tableEnv.sqlQuery`执行sql语句
    val table: Table = tableEnv.sqlQuery(sql)
    //11. 将SQL的执行结果转换成DataStream再打印出来
    val result: DataStream[Row] = tableEnv.toAppendStream[Row](table)
    //12. 启动流处理程序
    result.print
    env.execute(this.getClass.getName)
  }
}
