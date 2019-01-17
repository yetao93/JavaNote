# Netty主要组件

## 服务端

- EventLoop
定义了 Netty 的核心抽象，用于处理连接的生命周期中所发生的事件。

- EventLoopGroup
事件处理，包括创建新的连接以及处理入站和出站数据。一个EventLoopGroup可以有N个EventLoop，每个EventLoop都只能绑定一个Thread，每个EventLoop可以有N个Channel。由此可知，给定的一个Channel的IO操作都是由同一个Thread完成的

- ServerBootstrap 
初始化服务端
 
- SocketAddress
服务端监听地址

- Channel
基本的 I/O 操作（bind()、connect()、read()和 write()）依赖于底层网络传输所提供的原语。在基于 Java 的网络编程中，其基本的构造是 Socket 类。但是 Channel 接口所提供的 API，大大地降低了直接使用 Socket 类的复杂性

- ChannelFuture
Netty 中所有的 I/O 操作都是异步的。因为一个操作可能不会立即返回，所以我们需要一种用于在之后的某个时间点确定其结果的方法。为此，Netty 提供了 ChannelFuture 接口，其 addListener()方法注册了一个 ChannelFutureListener，以便在某个操作完成时（无论是否成功）得到通知。所有属于同一个 Channel 的操作都被保证其将以它们被调用的顺序被执行。

- ChannelHandler
充当了所有处理入站和出站数据的应用程序逻辑的容器，你的应用程序的业务逻辑通常驻留在一个或者多个 ChannelHandler 中。业务逻辑都写在这里面。

- ChannelPipeline
提供了 ChannelHandler 链的容器，当 Channel 被创建时，它会被自动地分配到它专属的 ChannelPipeline。
一个消息或者任何其他的入站事件被读取，那么它会从 ChannelPipeline 的头部开始流动，并被传递给第一个 ChannelInboundHandler。这个 ChannelHandler 不一定会实际地修改数据，具体取决于它的具体功能，在这之后，数据将会被传递给链中的下一个ChannelInboundHandler。最终，数据将会到达 ChannelPipeline 的尾端，届时，所有
处理就都结束了。
数据的出站运动（即正在被写的数据）在概念上也是一样的。在这种情况下，数据将从 ChannelOutboundHandler 链的尾端开始流动，直到它到达链的头部为止。在这之后，出站数据将会到达网络传输层，这里显示为 Socket。通常情况下，这将触发一个写操作。
通过使用作为参数传递到每个方法的 ChannelHandlerContext，事件可以被传递给当前 ChannelHandler 链中的下一个 ChannelHandler。
在 Netty 中，有两种发送消息的方式。你可以直接写到 Channel 中，也可以 写到和 Channel-Handler相关联的ChannelHandlerContext对象中。前一种方式将会导致消息从Channel-Pipeline 的尾端开始流动，而后者将导致消息从 ChannelPipeline 中的下一个 Channel-Handler 开始流动

- ChannelInitializer
**关键点**：当一个新的连接被接受时，一个新的子 Channel 将会被创建，而 ChannelInitializer 将会把一个你的 ChannelHandler 的实例添加到该 Channel 的 ChannelPipeline 中


![](https://i.imgur.com/60rCHWs.png)