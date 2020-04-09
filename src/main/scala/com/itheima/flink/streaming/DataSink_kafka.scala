package com.itheima.flink.streaming

import java.util.Properties

import com.itheima.flink.streaming.MysqlSource.CustomMysqlSource
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka. FlinkKafkaProducer011
import org.apache.kafka.clients.producer.ProducerConfig

object DataSink_kafka {
  def main(args: Array[String]): Unit = {
    // 1. 创建流处理环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 2. 设置并行度
    env.setParallelism(1)
    // 3. 添加自定义MySql数据源
    val source: DataStream[MysqlSource.User] = env.addSource(new CustomMysqlSource)

    // 4. 转换元组数据为字符串
    val strDataStream: DataStream[String] = source.map(
      line => line.id + line.username + line.password + line.name
    )

    //5. 构建FlinkKafkaProducer010  //kafka-clients.jar ProducerConfig
    val p: Properties = new Properties
    p.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "node01:9092,node02:9092,node03:9092")
    //val sink = new FlinkKafkaProducer010[String]("kafkatopic", new SimpleStringSchema(), p)
    //new FlinkKafkaProducer011[String]("kafkatopic",new SimpleStringSchema(),p,FlinkKafkaProducer011.Semantic.EXACTLY_ONCE)
    // 6. 添加sink
    //strDataStream.addSink(sink)
    // 7. 执行任务
    env.execute("flink-kafka-wordcount")
  }
}
