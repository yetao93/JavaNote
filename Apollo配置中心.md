# 1.总体设计 #

1. 用户在配置中心对配置进行修改并发布
2. 配置中心通知Apollo客户端有配置更新
3. Apollo客户端从配置中心拉取最新的配置、更新本地配置并通知到应用

![](https://i.imgur.com/fj92Xg7.png)

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

![](https://i.imgur.com/RBkaQnN.png)

三个辅助服务发现模块：
// TODO 以后再深入了解


# 2.各个击破 #

深入了解四大模块的设计理念和实现细节

## 2.1 AdminService ##

1. AdminService在配置发布后会往ReleaseMessage表插入一条消息记录，消息内容就是配置发布的AppId+Cluster+Namespace，参见DatabaseMessageSender


## 2.2 ConfigService ##

1. Config Service有一个线程会每秒扫描一次ReleaseMessage表，看看是否有新的消息记录，参见ReleaseMessageScanner

2. Config Service如果发现有新的消息记录，那么就会通知到所有的消息监听器（ReleaseMessageListener），如NotificationControllerV2，消息监听器的注册过程参见ConfigServiceAutoConfiguration

3. NotificationControllerV2得到配置发布的AppId+Cluster+Namespace后，会通知对应的客户端

## 2.3 Client ##

## 2.4 Portal ##

# 3.我的疑问 #

Q：有Eureka注册中心，为什么还需要MetaServer来做服务发现？
A：会在服务服务发现模块里面介绍，稍安勿躁

---

Q：