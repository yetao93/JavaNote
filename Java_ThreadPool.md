# 为什么使用线程池

线程是稀缺资源，如果被无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，合理的使用线程池对线程进行统一分配、调优和监控，有以下好处：
1. 降低资源消耗；
2. 提高响应速度；
3. 提高线程的可管理性；

Java1.5中引入的Executor框架把任务的提交和执行进行解耦，只需要定义好任务，然后提交给线程池，而不用关心该任务是如何执行、被哪个线程执行，以及什么时候执行。

# 线程池工作流程

当一个任务提交至线程池之后
1. 线程池首先判断核心线程池里的线程是否已经满了。如果不是，则创建一个新的工作线程来执行任务。否则进入2.
2. 判断工作队列是否已经满了，倘若还没有满，将线程放入工作队列。否则进入3.
3. 判断线程池里的线程是否都在执行任务。如果不是，则创建一个新的工作线程来执行。如果线程池满了，则交给饱和策略来处理任务。

# 线程池原理

## 内部状态

    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

32位的整型 ctl 功能非常强大，利用低29位表示线程数，高3位表示运行状态。共有5种状态：

- RUNNING：       高3位为111，负数，该状态的线程池会接收新任务，并处理阻塞队列中的任务；
- SHUTDOWN：   高3位为000，该状态的线程池不会接收新任务，但会处理阻塞队列中的任务；
- STOP ：             高3位为001，该状态的线程不会接收新任务，也不会处理阻塞队列中的任务，而且会中断正在运行的任务；
- TIDYING ：        高3位为010，所有的任务都已经终止；
- TERMINATED： 高3位为011，terminated()方法已经执行完成；

![java_threadpool_status](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_threadpool_status.png "java_threadpool_status")

## 源码分析

### execute()

当向线程池中提交一个任务，线程池会如何处理该任务？

![java_threadpool_execute](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_threadpool_execute.png "java_threadpool_execute")

1. workerCountOf方法根据ctl的低29位，得到线程池的当前线程数，如果线程数小于corePoolSize，则执行addWorker方法创建新的线程执行任务；否则执行步骤（2）；
2. 如果线程池处于RUNNING状态，且把提交的任务成功放入阻塞队列中，则执行步骤（3），否则执行步骤（4）；
3. 再次检查线程池的状态，如果线程池没有RUNNING，且成功从阻塞队列中删除任务，则执行reject方法处理任务；如果线程数为0，则执行addWorker方法创建新的线程；
4. 如果放入阻塞队列失败，则直接执行addWorker方法创建新的线程执行任务，如果addWoker执行失败，则执行reject方法处理任务；

![java_threadpool_execute_process](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_threadpool_execute_process.png "java_threadpool_execute_process")

#### 为什么需要再次检查线程池状态

主要目的是判断刚加入阻塞队列的task是否能被执行
1. 线程池在No.1371检查状态之后，可能被关闭了。这时需要回滚任务加入队列操作，将任务移出。
2. 因为可能存在有些线程在No.1366检查线程数之后死了，而如果所有线程都死了，任务虽然成功添加到队列中，但是不会有线程来执行。这时需要新开启一个线程。

### addWorker()

主要负责创建新的线程并执行任务

![java_threadpool_addworker_1](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_threadpool_addworker_1.png "java_threadpool_addworker_1")

这是addWoker方法实现的前半部分，判断是否可以创建线程：
1. 判断线程池的状态，如果线程池的状态值大于或等SHUTDOWN，即非RUNNING状态，则不处理提交的任务，直接返回失败；
2. 通过参数core判断当前需要创建的线程是否为核心线程，并判断线程数量是否达到最大值，如果没有达到则No.919使用CAS操作给线程数加1，成功后跳出循环开始创建新的线程；增加线程数失败则继续循环判断。

![java_threadpool_addworker_2](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_threadpool_addworker_2.png "java_threadpool_addworker_2")

这是addWoker方法实现的后半部分，创建线程的具体实现：

工作线程通过Woker类实现，在ReentrantLock锁的保证下，把Woker实例插入到HashSet后，并启动Woker中的线程，其中Worker类设计如下：
1. 继承了AQS类，可以方便的实现工作线程的中止操作；
2. 实现了Runnable接口，可以将自身作为一个任务在工作线程中执行；
3. 当前提交的任务firstTask作为参数传入Worker的构造方法；

        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

线程工厂在创建线程thread时，将Woker实例本身this作为参数传入，当执行No.957的start方法启动线程thread时，本质是执行了runWorker方法。

### runWorker() 线程池的核心

![java_threadpool_runworker](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_threadpool_runworker.png "java_threadpool_runworker")

runWorker方法是线程池的核心：
1. 线程启动之后，通过unlock方法释放锁，设置AQS的state为0，表示运行中断；
2. 获取第一个任务firstTask，执行任务的run方法，不过在执行任务之前，会进行加锁操作，任务执行完会释放锁；
3. 在执行任务的前后，可以根据业务场景自定义beforeExecute和afterExecute方法；
4. firstTask执行完成之后，通过getTask方法从阻塞队列中获取等待的任务，如果队列中没有任务，getTask方法会被阻塞并挂起，不会占用cpu资源；

PS：这里面的加锁和AQS释放锁，还不懂

### getTask() 获取任务

返回null表示这个worker要结束了，这种情况下workerCount-1

![java_threadpool_gettask](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_threadpool_gettask.png "java_threadpool_gettask")

执行流程：
1. 首先判断是否可以满足从workQueue中获取任务的条件，不满足return null
    A、线程池状态是否满足：
        （a）shutdown状态 + workQueue为空 或 stop状态，都不满足，因为被shutdown后还是要执行workQueue剩余的任务，但workQueue也为空，就可以退出了
        （b）stop状态，shutdownNow()操作会使线程池进入stop，此时不接受新任务，中断正在执行的任务，workQueue中的任务也不执行了，故return null返回
    B、线程数量是否超过maximumPoolSize 或 获取任务是否超时
        （a）线程数量超过maximumPoolSize可能是线程池在运行时被调用了setMaximumPoolSize()被改变了大小，否则已经addWorker()成功不会超过maximumPoolSize
        （b）如果 当前线程数量>corePoolSize，才会检查是否获取任务超时，这也体现了当线程数量达到maximumPoolSize后，如果一直没有新任务，会逐渐终止worker线程直到corePoolSize
2. 如果满足获取任务条件，根据是否需要定时获取调用不同方法：
    A、workQueue.poll()：如果在keepAliveTime时间内，阻塞队列还是没有任务，返回null
    B、workQueue.take()：如果阻塞队列为空，当前线程会被挂起等待；当队列中有任务加入时，线程被唤醒，take方法返回任务
3. 在阻塞从workQueue中获取任务时，可以被interrupt()中断，代码中捕获了InterruptedException，重置timedOut为初始值false，再次执行第1步中的判断，满足就继续获取任务，不满足return null，会进入worker退出的流程


## Runnable、Callable、Future

通过ExecutorService.submit()方法提交的任务，可以获取任务执行完的返回值。

在实际业务场景中，Future和Callable基本是成对出现的，Callable负责产生结果，Future负责获取结果。
1. Callable接口类似于Runnable，只是Runnable没有返回值。
2. Callable任务除了返回正常结果之外，如果发生异常，该异常也会被返回，即Future可以拿到异步执行任务各种结果；
3. Future.get方法会导致主线程阻塞，直到Callable任务执行完成；

### FutureTask

实现了Runnable、Future接口（可能是它们最简单的一个实现类），其中有个属性的类型为Callable，还有个outcome的Object类型作为返回值。

![java_futuretask_run](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_futuretask_run.png "java_futuretask_run")

其run方法的实现中，No.266执行Callable，获取到返回值result，设置到outcome中。

通过Future的实现方法，判断任务是否执行完，获取结果。
