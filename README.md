---
typora-root-url: assets
typora-copy-images-to: assets
---

# flink基础学习

​        **Apache Flink** 是一个框架和分布式处理引擎，用于在*无边界和有边界*数据流上进行有状态的计算。Flink 能在所有常见集群环境中运行，并能以内存速度和任意规模进行计算。

基于flink基础的学习的业务包括：

- Flink的批处理Source
  *   基于本地集合
  *   基于文件（本地文件/hdfs文件）
  *   基于CSV
  *   基于压缩文件
- Flink的Transformation
  *   map
  *   flatmap
  *   filter
  *   reduce
  *   rebalance
- Flink的Sink
  *   写入集合
  *   写入文件
- Flink程序本地执行和集群执行
- Flink的广播变量
- Flink的累加器
- Flink的分布式缓存

- Flink流处理的Source
  - 基于集合
  - 基于文件
  - 基于Socket
  - 自定义数据源
  - 使用Kafka作为数据源
  - 使用MySql作为数据源
- Flink流处理的Transformation
  - keyby
  - connect
  - split和select
- Flink流处理的Sink
  - sink到kafka
  - sink到mysql
- Flink的Window操作
  - 时间窗口
  - 计数窗口
  - 自定义窗口
- Flink的水印机制

- Flink的状态管理

  - keyed state
  - operator state

- Flink的CheckPoint

  - checkpoint的持久化方案
  - checkpoint持久化开发

- Flink的End-to-End Exactly-Once语义
  - flink应用的仅一次处理
  - flink实现的仅一次应用
  - flink中的两段提交

- Flink SQL & Table API

  - DataSet/DataStream转Table
  - Table转DataSet/DataStream
  - SQL操作数据
  - TableAPI操作数据