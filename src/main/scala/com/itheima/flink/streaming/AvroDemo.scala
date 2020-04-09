package com.itheima.flink.streaming

import java.util.Properties

import org.apache.avro.Schema
import org.apache.avro.Schema.Type
import org.apache.avro.generic.GenericRecord
import org.apache.flink.formats.avro.AvroDeserializationSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.kafka.clients.consumer.ConsumerConfig

object AvroDemo {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val schema = Schema.create(Type.STRING)
    val props = new Properties
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"node01:9092")
    env.addSource(new FlinkKafkaConsumer010[GenericRecord]("test",AvroDeserializationSchema
      .forGeneric(schema)
      ,props))
  }
}
