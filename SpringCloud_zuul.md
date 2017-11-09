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

### zuul自带的几个filter
zuul默认加载的filter有： pre类型的filter5个，route的3个，post的2个，共10个filter

route:

1. RibbonRoutingFilter 用注册中心并且serviceId不为null时启用
2. **SimpleHostRoutingFilter** 将请求转发给真正的处理它的地方，核心filter，一般都启用
3. SendForwardFilter 只有当请求上下文包含`ctx.containsKey("forward.to")`才启用

## ZuulServlet
`public class ZuulServlet extends HttpServlet`

Zuul基于Servlet框架，ZuulServlet用于处理所有的Request。其可以认Http Request的入口。 是单例的。

如果对SpringMVC比较熟悉，可以把ZuulServlet类比为DispatcherServlet，所有的Request都要经过ZuulServlet的处理。因此ZuulServlet是zuul框架源码分析的入口点。 具体下来是`ZuulServlet.service(ServletRequest servletRequest, ServletResponse servletResponse)`方法。

RequestContext提供了执行filter Pipeline所需要的Context，因为Servlet是单例多线程，这就要求RequestContext即要线程安全又要Request安全。context使用ThreadLocal保存，这样每个worker线程都有一个与其绑定的RequestContext，因为worker仅能同时处理一个Request，这就保证了Request Context 即是线程安全的由是Request安全的。所谓Request 安全，即该Request的Context不会与其他同时处理Request冲突。 

三个核心的方法preRoute(),route(), postRoute()，zuul对request处理逻辑都在这三个方法里，ZuulServlet交给ZuulRunner去执行。由于ZuulServlet是单例，因此ZuulRunner也仅有一个实例。

### ZuulRunner

ZuulRunner直接将执行逻辑交由FilterProcessor处理。 FilterProcessor也是单例。

### FilterProcessor

其功能就是依据filterType执行filter的处理逻辑 。核心方法runFilters()

- 首先根据Type获取所有输入该Type的filter，List<ZuulFilter> list。
- 遍历该list，执行每个filter的处理逻辑，processZuulFilter(ZuulFilter filter)
- RequestContext对每个filter的执行状况进行记录，应该留意，此处的执行状态主要包括其执行时间、以及执行成功或者失败，如果执行失败则对异常封装后抛出。 

到目前为止，zuul框架对每个filter的执行结果都没有太多的处理，它没有把上一filter的执行结果交由下一个将要执行的filter，仅仅是记录执行状态，如果执行失败抛出异常并终止执行。

## filter管理和动态刷新

暂缺