package com.itheima.flink.state

import java.sql.{Connection, DriverManager}
import java.util.Properties

import org.apache.flink.api.common.ExecutionConfig
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.typeutils.base.VoidSerializer
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.sink.{SinkFunction, TwoPhaseCommitSinkFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.kafka.clients.consumer.ConsumerConfig

object StreamTwoPhaseSinkMysqlDemo {
  def main(args: Array[String]): Unit = {
    val params: ParameterTool = ParameterTool.fromArgs(args)
    val value: String = params.get("kafka.topic","kafkatopic")
    println(value)
    /**
     * 1.创建流处理环境
     * 2.设置并行度
     * 3.开启checkpoint，设置相关参数
     * 4.设置kafka参数
     * 5.新建flinkkafkaconsumer
     * 6.添加kafka消费者
     * 7.wordcount的转换操作
     * 8.实现两段提交
     * 9.将结果写入到mysql
     * 10.开启任务执行
     */
    //1.创建流处理环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //2.设置并行度
    env.setParallelism(1)
    //3.开启checkpoint，设置相关参数
    env.enableCheckpointing(5000)
    //3.1开启重启策略
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(5, 5000))
    //3.2 当程序取消了，是否继续保存当前checkpoint，但是需要手动删除checkpoint
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    //3.3 两个checkpoint之间的间隔时间
    env.getCheckpointConfig.setCheckpointInterval(2000)
    //3.4 checkpoint的超时时间
    env.getCheckpointConfig.setCheckpointTimeout(60000)
    //3.5 如果checkpoint错误，是否整个任务失败
    env.getCheckpointConfig.setFailOnCheckpointingErrors(false)
    //3.6 checkpoint的目录
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(2)
    //3.7 保存到fs上
    env.setStateBackend(new FsStateBackend("hdfs://node01:8020/flink-checkpoints/"))
    //4. 设置kafka参数
    val props = new Properties
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "node01:9092,node02:9092,node03:9092")
    //设置消费组
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafkaconsumer0317")
    // 设置不自动提交offset
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false")
    //5.新建flinkkafkaconsumer
    val flinkKafkaConsumer = new FlinkKafkaConsumer010[String](
      args(0),
      new SimpleStringSchema(),
      props
    )
    //设置不让自动提交
    flinkKafkaConsumer.setCommitOffsetsOnCheckpoints(true)

    import org.apache.flink.api.scala._
    //6.添加kafka消费者
    val dataStream: DataStream[String] = env.addSource(flinkKafkaConsumer)
    //7.wordcount的转换操作
    val wordCount: DataStream[(String, Int)] = dataStream.flatMap(_.split("\\s+"))
      .filter(_.nonEmpty)
      .map(_ -> 1)
      .keyBy(_._1)
      .reduce((a, b) => (a._1, a._2 + b._2))
    //9.将结果写入到mysql
    wordCount.addSink(new SinkMysqlTwoPhaseCommit())
    //10.开启任务执行
    env.execute()
  }

  class MySqlConnectionState(@transient val connection: Connection) {
  }

  //8.实现两段提交
  class SinkMysqlTwoPhaseCommit() extends TwoPhaseCommitSinkFunction[(String, Int)
    , MySqlConnectionState, Void](new KryoSerializer(classOf[MySqlConnectionState], new ExecutionConfig), VoidSerializer.INSTANCE) {
    //往数据库插入数据
    override def invoke(transaction: MySqlConnectionState, value: (String, Int), context: SinkFunction.Context[_]): Unit = {
      try {
        val sql="insert into t_wordcount(word,counts) values(?,?) ON DUPLICATE KEY UPDATE counts=?"
        val ps = transaction.connection.prepareStatement(sql)
        ps.setString(1, value._1)
        ps.setInt(2, value._2)
        ps.setInt(3, value._2)
        ps.executeUpdate()
        ps.close()
      } catch {
        case ex:Exception =>
          ex.printStackTrace()
      }
    }

    //开启事务之前需要配置的属性
    override def beginTransaction(): MySqlConnectionState = {
      println(">>>>>>>>beginTransaction>>>>>>>>>>>>>")
      Class.forName("com.mysql.jdbc.Driver")
      val url="jdbc:mysql://192.168.139.160:3306/test?characterEncoding=utf-8"
      val username="root"
      val password="123456"
      val conn = DriverManager.getConnection(url, username, password)
      conn.setAutoCommit(false)
      new MySqlConnectionState(conn)
    }

    //预提交
    override def preCommit(transaction: MySqlConnectionState): Unit = {
      println(">>>>>>>>>>>>>>>>>preCommit>>>>>>>>>>>>>>")
    }

    //事务提交
    override def commit(transaction: MySqlConnectionState): Unit = {
      println(">>>>>>>>>>>commit>>>>>>>>>>>>>>")
      transaction.connection.commit()
      //transaction.connection.close()
    }

    //报错了，事务回滚机制
    override def abort(transaction: MySqlConnectionState): Unit = {
      println(">>>>>>abort>>>>>>>")
      transaction.connection.rollback()
      transaction.connection.close()
    }
  }
}