# RocketMQ

## 为什么使用消息队列

- 应用解耦。如果各个子系统间的耦合性太高，整体系统的可用性就会大幅降低，多个低错误率的子系统强耦合在一起，会得到高错误率的整体系统。
- 流量消峰。面对突发的大流量，超出系统自身的处理能力，可以使用消息队列作为缓冲。
- 消息分发。多个业务系统需要数据，生产方可以将数据写入消息队列，消费者根据各自的需求订阅即可。
- 保证最终一致性，使用事务消息。
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

提供轻量级的服务发现和路由。每个NameServer记录所有的路由信息，提供相同的读写服务和支持快速的存储扩展。基本是无状态的，并且是集群部署。

PS，类似于Eurake、zk之类的注册中心，为什么不直接用呢，因为它们功能太多，这里很多用不到。力求简单。

主要有两个功能：

- Broker管理。NameServer接受Broker的注册并且通过心跳机制检测其是否存活。
- 路由管理。每个NameServer拥有全部的Broker信息和queue信息。客户端在这里获得queue的路由信息。

**Broker**

采用主从结构。通过提供轻量级的 TOPIC 和 QUEUE 机制来完成消息的存储。支持 PUSH 和 PULL 模型，包含容错机制（数个副本），提供强大的削峰填谷能力，积累千亿条消息并保持它们的顺序。也有灾难恢复、丰富的指标统计和告警机制。

## 从生产者开始

### 消息类型

- 普通消息：消息队列 RocketMQ 中无特性的消息，区别于有特性的定时/延时消息、顺序消息和事务消息。又分同步发送和异步发送。
- 事务消息：实现类似 X/Open XA 的分布事务功能，以达到事务最终一致性状态。
- 定时/延时消息：允许消息生产者对指定消息进行定时（延时）投递。
- 顺序消息：允许消息消费者按照消息发送的顺序对消息进行消费。

#### 事务消息

消息队列 RocketMQ 提供类似 X/Open XA 的分布式事务功能，通过消息队列 RocketMQ 事务消息能达到分布式事务的最终一致。[详细介绍](https://www.cnblogs.com/hzmark/p/rocket_txn.html "详细介绍")

半事务消息：暂不能投递的消息，发送方已经成功地将消息发送到了消息队列 RocketMQ 服务端，但是服务端未收到生产者对该消息的二次确认，此时该消息被标记成“暂不能投递”状态，处于该种状态下的消息即半事务消息。

消息回查：由于网络闪断、生产者应用重启等原因，导致某条事务消息的二次确认丢失，消息队列 RocketMQ 服务端通过扫描发现某条消息长期处于“半事务消息”时，需要主动向消息生产者询问该消息的最终状态（Commit 或是 Rollback），该询问过程即消息回查。

消息队列 RocketMQ 事务消息交互流程如下所示。

![rmq-transaction-msg](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/rmq-transaction-msg.png "rmq-transaction-msg")

事务消息发送步骤如下：

1. 发送方将半事务消息发送至消息队列 RocketMQ 服务端。
2. RocketMQ 服务端将消息持久化成功之后，向发送方返回 Ack 确认消息已经发送成功，此时消息为半事务消息。
3. 发送方开始执行本地事务逻辑。
4. 发送方根据本地事务执行结果向服务端提交二次确认（Commit 或是 Rollback），服务端收到 Commit 状态则将半事务消息标记为可投递，订阅方最终将收到该消息；服务端收到 Rollback 状态则删除半事务消息，订阅方将不会接受该消息。

事务消息回查步骤如下：

1. 在断网或者是应用重启的特殊情况下，上述步骤 4 提交的二次确认最终未到达服务端，经过固定时间后服务端将对该消息发起消息回查。
2. 发送方收到消息回查后，需要检查对应消息的本地事务执行的最终结果。
3. 发送方根据检查得到的本地事务的最终状态再次提交二次确认，服务端仍按照步骤 4 对半事务消息进行操作。

缺点：一次消息发送需要两次请求。业务系统需要实现消息状态回查功能。消费失败，需要人工处理。

#### 顺序消息

顺序消息（FIFO 消息）是消息队列 RocketMQ 提供的一种严格按照顺序来发布和消费的消息。顺序发布和顺序消费是指对于指定的一个 Topic，生产者按照一定的先后顺序发布消息；消费者按照既定的先后顺序订阅消息，即先发布的消息一定会先被客户端接收到。

顺序消息分为全局顺序消息和分区顺序消息。顺序消息不支持事务，不支持定时/延时，只支持同步发送，不支持可靠异步发送，不支持单向Oneway发送。

[详细介绍](https://www.cnblogs.com/hzmark/p/orderly_message.html "详细介绍")

在MQ的模型中，顺序需要由3个阶段去保障：

1. 消息被发送时保持顺序。用户应该在同一个线程中采用同步的方式发送。将消息路由到特定的分区，通过MessageQueueSelector来实现分区的选择
2. 消息被存储时保持和发送的顺序一致，在同一个Message Queue中储存。存储时在空间上保持顺序。
3. 消息被消费时保持和存储的顺序一致。消息到达消费者后，必须按照顺序处理。

##### 全局顺序消息（基本不用）

对于指定的一个 Topic，所有消息按照严格的先入先出（FIFO）的顺序来发布和消费。

适用场景：适用于性能要求不高，所有的消息严格按照 FIFO 原则来发布和消费的场景。

##### 分区顺序消息

对于指定的一个 Topic，所有消息根据 Sharding Key 进行区块分区。同一个分区内的消息按照严格的 FIFO 顺序进行发布和消费。Sharding Key 是顺序消息中用来区分不同分区的关键字段，和普通消息的 Key 是完全不同的概念。

![rmq-part-sequence-msg](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/rmq-part-sequence-msg.png "rmq-part-sequence-msg")

适用场景：适用于性能要求高，以 Sharding Key 作为分区字段，在同一个区块中严格地按照 FIFO 原则进行消息发布和消费的场景。

### Producer的负载均衡

Producer轮询Topic下的所有Message Queue的方式来实现发送方的负载均衡。失败了选择另一个queue进行重试。

而由于queue可以散落在不同的broker，所以消息就发送到不同的broker下。

### 消息发送方式和返回状态

发送方式：

- 可靠同步发送。同步发送是指消息发送方发出数据后，会在收到接收方发回响应之后才发下一个数据包的通讯方式。
- 可靠异步发送。异步发送是指发送方发出数据后，不等接收方发回响应，接着发送下个数据包的通讯方式。需要实现回调接口接收服务器的相应，并对响应处理。
- 单向（Oneway）发送。发送方只负责发送消息，不等待服务器回应且没有回调函数触发。 此方式发送消息的过程耗时非常短，一般在微秒级别。

返回状态：

- FLUSH_DISK_TIMEOUT， 没有在规定的时间内完成刷盘（broker的刷盘策略被设置成SYNC_FLUSH才会报这个错）
- FLUSH_SLAVE_TIMEOUT，在主备方式下，并且broker设置为SYNC_MASTER时，没有在设定时间内完成主从同步
- SLAVE_NOT_AVAILABLE，在主备方式下，并且broker设置为SYNC_MASTER时，没有找到SLAVE
- SEND_OK，            发送成功，具体消息是否已经储存到磁盘等要看刷盘策略、主从策略。

## 消息在Broker存储

### Broker的高可用和可靠性

对于一个Topic而言，它可以分布在多个Master Broker上，这样在其中一个Broker不可用之后，其他Broker依旧可以提供服务，不影响写入。

在一个Master Broker挂掉之后，会将Master上读取请求转移到Slave，保证系统可用。Master-Slave之间有同步复制和异步复制，异步复制效率高，但是**会存在少量数据还没有从Master复制到Slave的情况**。

RMQ有同步刷盘和异步刷盘两种持久化方式来写入消息。唯一差别是异步刷盘写完pagecache直接返回，而同步刷盘需要等待刷盘完成之后才返回。同步刷盘必然耗时要比异步刷盘要大。写入流程如下：

1. 写入pagecache，线程等待，通知刷盘线程进行刷盘
2. 刷盘线程刷盘后，唤醒前端等待线程，可能是一批线程
3. 前端等待线程想用户返回写入结果

### Broker中的队列模型

![rmq-message-queue-model](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/rmq-message-queue-model.png "rmq-message-queue-model")

- 一个Topic由多个Partition组成，每个Partition是一个队列，即一个Partition内部的消息是先进先出的（顺序的）
- Producer会写多个Partition，Consumer也会去读多个Partition
- Producer不断的向Partition末尾追加消息，Consumer从队列的头开始一直向后读取消息。

### commit log

虽然每个topic下面有很多message queue，但是message queue本身并不存储消息。真正的消息存储会写在CommitLog的文件，message queue只是存储CommitLog中对应的位置信息，方便通过message queue找到对应存储在CommitLog的消息。

不同的topic，message queue都是写到相同的CommitLog 文件，也就是说CommitLog完全的顺序写。

PS，这是重点，需要更详细



## 来到消费者

### Push 和 Pull

https://www.cnblogs.com/hzmark/p/mq_push_pull.html

### 集群消费和广播消费

https://help.aliyun.com/document_detail/43163.html?spm=a2c4g.11186623.2.28.6fd5208dvYRv9E

### Consumer的负载均衡

每当消费者实例的数量有变更，都会触发一次所有实例的负载均衡，这时候会按照queue的数量和实例的数量平均分配queue给每个实例。

集群模式下，一个queue只分给一个consumer实例，一个consumer实例可以允许同时分到不同的queue。

但是如果consumer实例的数量比message queue的总数量还多的话，多出来的consumer实例将无法分到queue，也就无法消费到消息，也就无法起到分摊负载的作用了。

广播模式下，由于广播模式下要求一条消息需要投递到一个消费组下面所有的消费者实例，所有consumer都分到所有的queue。

### 重试和死信队列

https://help.aliyun.com/knowledge_detail/54318.html?spm=a2c4g.11186631.2.3.2bd9805c9xBxz2
https://help.aliyun.com/document_detail/87277.html?spm=a2c4g.11186623.2.30.6fd5208dvYRv9E


### 启动从哪里开始消费

当新实例启动的时候，Consumer 会拿到本消费组broker已经记录好的消费进度（consumer offset），按照这个进度发起自己的第一次Pull请求。

如果这个消费进度在Broker并没有存储起来，证明这个是一个全新的消费组，这时候客户端有几个策略可以选择：

- CONSUME_FROM_LAST_OFFSET //默认策略，从该队列最尾开始消费，即跳过历史消息
- CONSUME_FROM_FIRST_OFFSET //从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
- CONSUME_FROM_TIMESTAMP//从某个时间点开始消费，和setConsumeTimestamp()配合使用，默认是半个小时以前

注意，这些策略只有在全新的消费组才会使用到。



















