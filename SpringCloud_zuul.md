# zuul
zuul是spring cloud集成的网关（api gateway），主要是做统一的权限控制，路由分发，反向代理和负载均衡。

zuul的核心是一系列的filters, 其作用可以类比Servlet框架的Filter，或者AOP。其原理就是在zuul把Request route到源web-service的时候，处理一些逻辑，比如Authentication，Load Shedding等。

![zuul核心](https://i.imgur.com/1ykZzW6.png)

## ZuulFilter
`ZuulFilter`抽象类实现了`IZuulFilter`接口，该抽象类有四个抽象方法，子类需要实现它们。

1. public String filterType()
2. public int filterOrder()
3. public boolean shouldFilter()
4. public Object run()

filter的功能并不具有太多特色，它和Servlet框架的Filter以及AOP功能及角色都很像，应该是zuul的开发者借鉴了这些优秀的设计。 

zuul框架主要的功能就是动态的读取，编译，运行这些filter。filter之间不直接communicate ，他们之间通过RequestContext来共享状态信息，既然filter都是对特定Request的处理，那么RequestContext就是Request的Context，RequestContext用来管理 Request的Context，不受其它Request的影响。 

### filter的管理和动态刷新
// TODO 暂缺

## ZuulServlet
`public class ZuulServlet extends HttpServlet`

Zuul基于Servlet框架，ZuulServlet用于处理所有的Request。其可以认Http Request的入口。 是单例的。

如果对SpringMVC比较熟悉，可以把ZuulServlet类比为DispatcherServlet，所有的Request都要经过ZuulServlet的处理。因此ZuulServlet是zuul框架源码分析的入口点。 具体下来是`ZuulServlet.service(ServletRequest servletRequest, ServletResponse servletResponse)`方法。

RequestContext提供了执行filter Pipeline所需要的Context，因为Servlet是单例多线程，这就要求RequestContext即要线程安全又要Request安全。context使用ThreadLocal保存，这样每个worker线程都有一个与其绑定的RequestContext，因为worker仅能同时处理一个Request，这就保证了Request Context 即是线程安全的由是Request安全的。所谓Request 安全，即该Request的Context不会与其他同时处理Request冲突。 

三个核心的方法preRoute(),route(), postRoute()，zuul对request处理逻辑都在这三个方法里，ZuulServlet交给ZuulRunner去执行。由于ZuulServlet是单例，因此ZuulRunner也仅有一个实例。

### ZuulRunner

ZuulRunner直接将执行逻辑交由FilterProcessor处理。 FilterProcessor也是单例。

### FilterProcessor

其功能就是依据filterType执行filter的处理逻辑 。核心方法`runFilters(String sType)`

- 首先根据Type获取所有输入该Type的filter，List<ZuulFilter> list。
- 遍历该list，执行每个filter的处理逻辑，processZuulFilter(ZuulFilter filter)
- RequestContext对每个filter的执行状况进行记录，应该留意，此处的执行状态主要包括其执行时间、以及执行成功或者失败，如果执行失败则对异常封装后抛出。 

到目前为止，zuul框架对每个filter的执行结果都没有太多的处理，它没有把上一filter的执行结果交由下一个将要执行的filter，仅仅是记录执行状态，如果执行失败抛出异常并终止执行。


## ZuulController
先由Spring MVC的`DispatchServlet.doDispatch`接收处理请求

在getHandler时，去ZuulHandlerMapping中查找是否有对应的路由，其父类的handlerMap属性储存了所有路由信息
		
查找到路由的，则交给ZuulController处理，再交给ZuulServlet处理，其中依次经过各级filter过滤

## zuul自带的几个filter
@EnableZuulProxy默认加载的filter有： pre类型的filter5个，route的3个，post的1个，error的1个，共10个filter

### pre:

#### ServletDetectionFilter - -3

启用条件： 永远为true

功能说明： 检测请求是在DispatcherServlet还是ZuulServlet中运行，检测结果放到ctx中，key为isDispatcherServletRequest

#### Servlet30WrapperFilter - -2
启用条件： 永远为true

功能说明： 将Servlet3.0的请求包装成Servlet2.5的，因为Zuul默认包装器只接收Servlet2.5
`ctx.setRequest(new Servlet30RequestWrapper(request));`

也许是这样使得每一次`request.getInputStream()`得到的ServletInputStream都是不同的对象。
`request.getReader();`得到的也是不同的BufferedReader对象，并且对于HttpServletRequest来说是不能同时调用这两个方法，但是在这里可以同时调用。

#### FormBodyWrapperFilter - -1
启用条件： 请求体的contentType不为null并且是能够支持的类型（application/x-www-form-urlencoded 即表单类型）

功能说明： parses form data and reencodes it for downstream services

#### DebugFilter - 1
启用条件： 请求中带有参数debug=true

功能说明： `ctx.set("debugRouting", true);ctx.set("debugRequest", true);`

#### PreDecorationFilter - 5
启用条件： `!ctx.containsKey("forward.to") && !ctx.containsKey("serviceId");`不转发，不含有serviceId

功能说明： 匹配路由信息，如果成功，往ctx存放requestURI（route.getPath()）、proxy（route.getId()）、retryable、routeHost。如果失败，ctx.set("forward.to", forwardURI);


### route:

#### RibbonRoutingFilter - 10

启用条件： 含有serviceId，不含routeHost，sendZuulResponse为true时启用，跟SimpleHostRoutingFilter互斥
`ctx.get("routeHost") == null && ctx.get("serviceId") != null && ctx.getBoolean("sendZuulResponse", true)`

功能说明： 有注册中心时用这个路由filter

#### SimpleHostRoutingFilter - 200

启用条件： 含有routeHost，sendZuulResponse为true时启用，
`ctx.get("routeHost") != null && ctx.getBoolean("sendZuulResponse", true)`

功能说明： 将请求转发给真正的处理它的地方，核心filter，返回值`set("zuulResponse", response)`

####  SendForwardFilter - 500

启用条件： 处理ctx中包含了forward.to时的情况，
`ctx.containsKey("forward.to") && !ctx.getBoolean("sendForwardFilter.ran", false)`

功能说明： 

### post:

#### SendResponseFilter - 1000

启用条件： 不包含throwable、并且响应头不为空或响应体不为空
`context.get("throwable") == null && (!context.getZuulResponseHeaders().isEmpty() || context.getResponseDataStream() != null || context.getResponseBody() != null)`

功能说明： 

### error:

#### SendErrorFilter - 0

启用条件： `ctx.get("throwable") != null && !ctx.getBoolean("sendErrorFilter.ran", false)`

功能说明： 


## 一些细节
1. FilterProcessor执行每一个注册的ZuulFilter，将执行状态记录在ctx中，key是executedFilters
`ServletDetectionFilter[SUCCESS][1ms], Servlet30WrapperFilter[SUCCESS][1ms], PreDecorationFilter[SUCCESS][1ms], RateLimitFilter[SUCCESS][4ms], SimpleHostRoutingFilter[SUCCESS][45ms], ResponseFilter[SUCCESS][0ms]`