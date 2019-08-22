# RocketMQ

## 为什么使用消息队列

- 应用解耦。如果各个子系统间的耦合性太高，整体系统的可用性就会大幅降低，多个低错误率的子系统强耦合在一起，会得到高错误率的整体系统。
- 流量消峰。面对突发的大流量，超出系统自身的处理能力，可以使用消息队列作为缓冲。
- 消息分发。多个业务系统需要数据，生产方可以将数据写入消息队列，消费者根据各自的需求订阅即可。
- 保证最终一致性，
- 方便动态扩容，

### RMQ 的介绍和特点

Apache RocketMQ 是一个分布式消息传递平台，具有低延迟、高性能和可靠性、万亿级容量和灵活的可伸缩性。

- 支持事务型消息（消息发送和DB操作保持两方的最终一致性，rabbitmq和kafka不支持）
- 支持结合rocketmq的多个系统之间数据最终一致性（多方事务，二方事务是前提）
- 支持18个级别的延迟消息（rabbitmq和kafka不支持）
- 支持指定次数和时间间隔的失败消息重发（kafka不支持，rabbitmq需要手动确认）
- 支持consumer端tag过滤，减少不必要的网络传输（rabbitmq和kafka不支持）
- 支持重复消费（rabbitmq不支持，kafka支持）

缺点：
Broker 仅提供了 Master 到 Slave 的复制，没有 Failover 切换的能力？
没有告警与监控体系？

Kafka 的特点：
更偏向大数据，日志处理。缺少死信，消费失败自动重试，事务消息，定时消息，消息过滤，广播消息等特性，另外 Kafka 没有同步刷盘。

## RMQ 的架构

![rmq-basic-arc](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/rmq-basic-arc.png "rmq-basic-arc")

包含四个部分，name servers, brokers, producers, consumers，它们都是可水平扩展的，避免单点故障。

**NameServer**

提供轻量级的服务发现和路由。每个NameServer记录所有的路由信息，提供相同的读写服务和支持快速的存储扩展。

PS，类似于Eurake、zk之类的注册中心，为什么不直接用呢，因为它们功能太多，这里很多用不到。力求简单。

主要有两个功能：

- Broker管理。NameServer接受Broker的注册并且通过心跳机制检测其是否存活。
- 路由管理。每个NameServer拥有全部的Broker信息和queue信息。客户端在这里获得queue的路由信息。

**Broker**

通过提供轻量级的 TOPIC 和 QUEUE 机制来完成消息的存储。支持 PUSH 和 PULL 模型，包含容错机制（数个副本），提供强大的削峰填谷能力，积累千亿条消息并保持它们的顺序。也有灾难恢复、丰富的指标统计和告警机制。





















