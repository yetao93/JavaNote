## Netty基本组成
 一般有×××Server、×××Serverhandler、×××Client、×××Clienthandler以及调用各种handler，详情参见Netty-io.netty.example.discard

## ByteBuf
 先说JDK ByteBuffer，为什么每次读写时要额外调用`flip()`和`clear()`等方法，否则将出错？

	ByteBuffer buffer = ByteBuffer.allocate(88);//分配88个字节容量的缓冲区
	String value="Netty权威指南";
	buffer.put(value.getBytes());				//将String类型转换为字节数组存入缓冲区
	buffer.flip();
	byte[] arrays = new byte[buffer.remaining()];//创建一个byte数组，大小为buffer当前position到limit
	buffer.get(arrays);							//将缓冲区里的数据存入byte数组
	String decode = new String(arrays);			//将byte数组转换为string类型
	System.out.println(decode);

如果不进行`flip()`操作，内存中buffer是这样的：0-Netty权威指南-position----limit。

进行`flip()`操作，内存:position-Netty权威指南-limit-----capacity。

ByteBuffer只有一个位置指针用于读写操作position，它操作的范围是position至limit

---

ByteBuf通过两个位置指针来协助缓冲区的读写操作，读使用readerIndex，写使用writerIndex，一开始都是0，写入数据会使writerIndex增加，读取数据会使readerIndex增加，但不会超过writerIndex，读取之后发生以下变化：

- 0-readerIndex的会被视为discard，调用`discardReadBytes()`可以释放这部分空间。
- readerIndex-writerIndex之间为可读部分，相当于ByteBuffer的position-limit，
- writerIndex-capacity之间为可写部分，相当于ByteBuffer的limit-capacity

可用`markReaderIndex()、markWriterIndex()`分别保存读写指针，读写后

再用`resetReaderIndex()、resetWriterIndex()`重置回原来的位置，读的时候比较有用

ByteBuf还有动态扩展机制，防止溢出。

**Bytebuf与string的互转**

把String类型转换为byte数组再写入ByteBuf中：

    byte[] bs = "yetaotao".getBytes();
    firstMessage = Unpooled.buffer(bs.length);
    firstMessage.writeBytes(bs);

把接收到的ByteBuf转换为byte数组再转换为String：

	ByteBuf buf = (ByteBuf) msg;
	byte[] bs = new byte[buf.readableBytes()];
	buf.readBytes(bs);//经过读取操作后，readerIndex会增加，最后等于writerIndex
	String str = new String(bs,"utf-8");

##EventLoop和EventLoopGroup
服务端启动时创建的两个NioEventLoopGroup实际是两个独立的Reactor线程池，逻辑隔离NIO Acceptor和NIO I/O线程。

一个用于接收客户端的TCP连接，初始化Channel参数；将链路状态变更事件通知给ChannelPipeline

另一个用于异步读取通信对端的数据包，发送读事件到ChannelPipeline；异步发送消息到通信对端，调用ChannelPipeline的消息发送接口；执行系统调用Task；执行定时任务Task，例如链路空闲状态检测定时任务。

通过调整线程池的线程个数，是否共享线程池等方式，Netty的Reactor线程模型可以在单线程、多线程和主从多线程之间切换。

	EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
    	ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 100)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new TelnetServerInitializer(sslCtx));




##ServerBootstrap和Bootstrap
都继承自AbstractBootstrap，这是一个帮助类，为了更方便启用Channel，为了方便，支持了method-chainning 也就是说函数都返回一个对自身的引用。


##ChannelPipeline
这是一个ChannelHandler的容器，内部维护了一个ChannelHandler的链表和迭代器，它负责ChannelHandler的管理和事件拦截与调度，可以方便地实现ChannelHandler查找、添加、替换和删除

用户不需要自己创建pipeline，因为使用ServerBootstrap或者Bootstrap启动服务端或客户端时，Netty会为每个Channel连接创建一个独立的pipeline，只要将handler加入其中即可

事件处理：底层的SocketChannel read()方法读取Bytebuf，触发ChannelRead事件，由I/O线程NioEventLoop调用ChannelPipeline的fireChannelRead(Object msg)方法，将消息传输到ChannelPipeline中，然后依次被handler处理

##ChannelHandler
Netty中的所有handler都实现自ChannelHandler接口。但是大多数ChannelHandler会选择性地拦截处理某些事件，而实现这个借口就要实现其所有方法，如此就会产生冗余和臃肿，所以可以选择继承自ChannelHandlerAdapter基类，只要选择性覆盖其中几个方法即可。常见的如：

	public interface ChannelHandler
	public abstract class ChannelHandlerAdapter implements ChannelHandler
	public class ChannelInboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelInboundHandler
	public abstract class SimpleChannelInboundHandler<I> extends ChannelInboundHandlerAdapter
	

按照输出输出来分，分为ChannelInboundHandler、ChannelOutboundHandler两大类。

ChannelInboundHandler对从客户端发往服务器的报文进行处理，一般用来执行解码、读取客户端数据、进行业务处理等；

ChannelOutboundHandler对从服务器发往客户端的报文进行处理，一般用来进行编码、发送报文到客户端。 

Netty中，可以注册多个handler。ChannelInboundHandler按照注册的先后顺序执行；ChannelOutboundHandler按照注册的先后逆序执行

***注意***

1、在没有任何encoder、decoder的情况下，Netty发送接收数据都是按照ByteBuf的形式，其它形式都是不合法的。

1、ChannelInboundHandler之间的传递，通过调用 ctx.fireChannelRead(msg) 实现；调用ctx.write(msg) 将传递到ChannelOutboundHandler。

2、ctx.write()方法执行后，需要调用flush()方法才能令它立即执行。

3、ChannelOutboundHandler 在注册的时候需要放在最后一个ChannelInboundHandler之前，否则将无法传递到ChannelOutboundHandler。也就是说最后一个handler必须是Inbound?

4、Handler的消费处理放在最后一个处理。 

---

**HttpServerCodec**：A combination of HttpRequestDecoder and HttpResponseEncoder which enables easier server side HTTP implementation.

**HttpRequestEncoder**：将HttpRequest或HttpContent编码成ByteBuf

**HttpRequestDecoder**：将ByteBuf解码成HttpRequest和HttpContent

**HttpResponseEncoder**：将HttpResponse或HttpContent编码成ByteBuf

**HttpResponseDecoder**：将ByteBuf解码成HttpResponse和HttpContent

##ChannelFuture
ChannelFuture用于获取异步操作的结果。在Netty中所有操作都是异步的，任何I/O调用都会立即返回，而不是等待操作完成。

ChannelFuture有两种状态：uncomplete和complete。

当开始一个I/O操作时，一个新的ChannelFuture被创建，此时它处于uncomplete状态，非失败非成功非取消，因为此时它还没有完成。当它完成时，ChannelFuture会被设置为complete，结果有三种：操作成功、操作失败、操作取消



## Http的一些东西（有待完善）

Http请求消息，由三部分组成：请求行、请求头、请求正文

	GET / HTTP/1.1
	Host: 127.0.0.1:8080
	User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0
	Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
	Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3
	Accept-Encoding: gzip, deflate
	DNT: 1
	Connection: keep-alive


HTTP响应也是由三个部分组成，分别是：状态行、响应头、响应正文

	HTTP/1.1 200 OK
	Content-Type: text/plain
	Content-Length: 11
	Connection: keep-alive

在浏览器访问对应主机及端口时，会发送请求，handler接收到后返回响应

	FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	//new DefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content)
	response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
	response.content().writeBytes(ByteBuf src);//响应正文
	ctx.write(response).addListener(ChannelFutureListener.CLOSE);


##Question
1. 如何关闭连接，不是直接按停止运行？
2. ServerBootstrap（Bootstrap sub-class which allows easy bootstrap of ServerChannel）具体配置？
3. Bootstrap（makes it easy to bootstrap a Channel to use for clients）具体配置？
4. `ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);`之后添加监听器是做什么？

---

1. Netty-example-io.netty.example.telnet以及localEcho这个项目有，客户端输入bye之后就能关闭连接。这两个都是在Server中不断循环接收输入，如果输入指定内容就跳出循环，以此关闭，但是我想在handler中关闭。其实就是`ctx.close();`也可以是服务端发送完最后的消息再关闭连接
`ChannelFuture future = ctx.write(response);`
`future.addListener(ChannelFutureListener.CLOSE);`
2. b.group.channel.handler.childHandler.option.childOption，b.bind
3. b.group.channel.handler，b.connect
4. 可以在向服务端发送完信息后关闭连接

