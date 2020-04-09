package com.itheima.flink.streaming

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaConsumerBase}
import org.apache.kafka.clients.consumer.ConsumerConfig

object KafkaDataSource {
  def main(args: Array[String]): Unit = {
    /**
     * 1. 创建流处理环境
     * 2. 指定链接kafka相关信息
     * 3. 创建kafka数据流(FlinkKafkaConsumer010)
     * 4. 添加Kafka数据源
     * 5. 打印数据
     * 6. 执行任务
     */
    //1. 创建流处理环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //开启checkpoint
    env.enableCheckpointing(5000)
//    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    //env.setStateBackend(new FsStateBackend("hdfs://node01:8020/flink-checkpoints"))
    //env.setStateBackend(new RocksDBStateBackend("hdfs://node01:8020/flink-checkpoints"))
    //2. 指定链接kafka相关信息
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "node01:9092,node02:9092,node03:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafkaConsumer")
    //动态获取kafka分区信息
    props.put(FlinkKafkaConsumerBase.KEY_PARTITION_DISCOVERY_INTERVAL_MILLIS, "5000")
    val topic = "kafkatopic"
    //3. 创建kafka数据流(FlinkKafkaConsumer010)
    val flinkKafkaConsumer = new FlinkKafkaConsumer010[String](topic, new SimpleStringSchema(), props)
    //出现故障，重启任务，从最早(最新的)的数据开始消费
    //基于某个时间戳开始消费
    //flinkKafkaConsumer.setStartFromTimestamp(1584842454000L)
    val partitionToLong = new java.util.HashMap[KafkaTopicPartition, java.lang.Long]
    val partition0 = new KafkaTopicPartition(topic,0);
    val partition1 = new KafkaTopicPartition(topic,1);
    partitionToLong.put(partition0,5L)
    partitionToLong.put(partition1,2L)
    flinkKafkaConsumer.setStartFromSpecificOffsets(partitionToLong)
    //4. 添加Kafka数据源
    val dataStream: DataStream[String] = env.addSource(flinkKafkaConsumer)
    //5. 打印数据
    dataStream.print()
    //6. 执行任务
    env.execute()
  }
}
