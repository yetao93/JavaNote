# 1.总体设计 #

1. 用户在配置中心对配置进行修改并发布
2. 配置中心通知Apollo客户端有配置更新
3. Apollo客户端从配置中心拉取最新的配置、更新本地配置并通知到应用

![Apollo架构图](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/apollo-basic-architecture.png)


## 1.1 架构模块 ##

四个核心模块及其主要功能：

1. **ConfigService**
 - 提供配置获取接口
 - 提供配置推送接口
 - 服务于Apollo客户端
2. **AdminService**
	- 提供配置管理接口
	- 提供配置修改发布接口
	- 服务于管理界面Portal
3.  **Apollo客户端-Client**
	- 为应用获取配置，支持实时更新
	- 通过MetaServer获取ConfigService的服务列表
	- 使用客户端软负载SLB方式调用ConfigService
4. **管理界面-Portal**
	- 配置管理界面
	- 通过MetaServer获取AdminService的服务列表
	- 使用客户端软负载SLB方式调用AdminService

![Apollo架构图](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/apollo-overall-architecture.png)

三个辅助服务发现模块：
// TODO 以后再深入了解


# 2.各个击破 #

深入了解四大模块的设计理念和实现细节

## 2.1 AdminService ##

1. AdminService在配置发布后会往ReleaseMessage表插入一条消息记录，消息内容就是配置发布的AppId+Cluster+Namespace，参见DatabaseMessageSender


## 2.2 ConfigService ##

1. Config Service有一个线程会每秒扫描一次ReleaseMessage表，看看是否有新的消息记录，参见ReleaseMessageScanner

2. Config Service如果发现有新的消息记录，那么就会通知到所有的消息监听器（ReleaseMessageListener），如NotificationControllerV2，消息监听器的注册过程参见ConfigServiceAutoConfiguration

3. NotificationControllerV2得到配置发布的AppId+Cluster+Namespace后，会通知对应的客户端。Config Service通知客户端的实现方式：**这里很难，我没有看懂**：
	- 客户端会发起一个Http请求到Config Service的notifications/v2接口，也就是NotificationControllerV2，参见RemoteConfigLongPollService。
	- NotificationControllerV2不会立即返回结果，而是通过Spring DeferredResult把请求挂起
	- 如果在60秒内没有该客户端关心的配置发布，那么会返回Http状态码304给客户端
	- 如果有该客户端关心的配置发布，NotificationControllerV2会调用DeferredResult的setResult方法，传入有配置变化的namespace信息，同时该请求会立即返回。客户端从返回的结果中获取到配置变化的namespace后，会立即请求Config Service获取该namespace的最新配置。

4. 配置读取接口，前面只是通知配置有变化，具体配置内容需要Client主动来读取。why？


## 2.3 Client ##
![](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/apollo-basic-architecture.png)

1. 客户端和服务端保持了一个长连接，从而能第一时间获得配置更新的推送。（通过Http Long Polling实现）
2. 客户端还会定时从Apollo配置中心服务端拉取应用的最新配置。
	- 这是一个fallback机制，为了防止推送机制失效导致配置不更新
	- 客户端定时拉取会上报本地版本，所以一般情况下，对于定时拉取的操作，服务端都会返回304 - Not Modified
	- 定时频率默认为每5分钟拉取一次，客户端也可以通过在运行时指定System Property: apollo.refreshInterval来覆盖，单位为分钟。
3. 客户端从Apollo配置中心服务端获取到应用的最新配置后，会保存在内存中
4. 客户端会把从服务端获取到的配置在本地文件系统缓存一份。在遇到服务不可用，或网络不通的时候，依然能从本地恢复配置
5. 应用程序可以从Apollo客户端获取最新的配置、订阅配置更新通知。可以通过API方式获取，Apollo也和Spring集成，直接使用@Value注解
6. 应用启动的时候，会扫描所有的bean解析其中的属性和要关心的注解（例如@Value)，将它们以placeHolder作为Key，bean+beanName+field等信息作为value，放入一个map中，待到有配置更新时（监听器）遍历这个map，对key相同的属性采用反射的方式进行更新。具体实现AutoUpdateConfigChangeListener、SpringValueProcessor

## 2.4 Portal ##

配置管理的页面，不多说。

# 3. 通用的知识 #

- ScheduledExecutorService 定时任务线程池，常用于创建一个线程跑定时任务
- guava里的工具类Strings、CollectionUtils、Splitter字符串分割（NotificationControllerV2-49）、Joiner字符串拼接（ReleaseMessageKeyGenerator-12）、Multimap用于代替复杂的集合类型比如Map<\String, List<\StudentScore>>、
- BlockingQueue offer添加满了不会报错，add会报错

# 4. 我的疑问 #

Q：有Eureka注册中心，为什么还需要MetaServer来做服务发现？
A：会在服务服务发现模块里面介绍，稍安勿躁

---

Q：