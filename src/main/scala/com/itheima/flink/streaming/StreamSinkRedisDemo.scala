package com.itheima.flink.streaming

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

object StreamSinkRedisDemo {
  def main(args: Array[String]): Unit = {
    //创建流处理环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //从socket获取数据
    val socketDataStream: DataStream[String] = env.socketTextStream("node01", 9999)
    // map操作
    val mapDataStream: DataStream[(String, String)] = socketDataStream.map(line => ("key", line))

    val conf = new FlinkJedisPoolConfig.Builder()
      .setHost("node01")
      .setPort(6379)
      .setDatabase(1)
      .build()
    //保存数据到redis
    /**
     * redis nosql数据库  内存数据库
     * key - value  （key,value）  (String, String)
     * 需要两个参数 FlinkJedisConfigBase RedisMapper
     * 实现三个方法：1.用哪个命令 LPUSH RPUSH SET STRING
     *            2.指定key
     *            3.指定value
     */
    mapDataStream.addSink(new RedisSink[(String, String)](conf, new RedisMapper[(String, String)] {
      override def getCommandDescription: RedisCommandDescription = {
        new RedisCommandDescription(RedisCommand.LPUSH)
      }
      override def getKeyFromData(t: (String, String)): String = {
        t._1
      }
      override def getValueFromData(t: (String, String)): String = {
        t._2
      }
    }))
    env.execute()
  }
}
