# Java垃圾回收

垃圾回收（Garbage Collection，GC），顾名思义就是释放垃圾占用的空间，防止内存泄露。有效的使用可以使用的内存，对内存堆中已经死亡的或者长时间没有使用的对象进行清除和回收。

## 怎么定义垃圾

既然我们要做垃圾回收，首先我们得搞清楚垃圾的定义是什么，哪些内存是需要回收的。

### 引用计数算法

引用计数算法（Reachability Counting）是通过在对象头中分配一个空间来保存该对象被引用的次数（Reference Count）。如果该对象被其它对象引用，则它的引用计数加1，如果删除对该对象的引用，那么它的引用计数就减1，当该对象的引用计数为0时，那么该对象就会被回收。

但如果存在相互引用，导致它们的引用计数永远都不会为0，通过引用计数算法，也就永远无法通知GC收集器回收它们。

### 可达性分析算法

可达性分析算法（Reachability Analysis）的基本思路是，通过一些被称为GC Roots的对象作为起点，从这些节点开始向下搜索，搜索走过的路径被称为引用链（Reference Chain)，当一个对象到 GC Roots 没有任何引用链相连时（即从 GC Roots 节点到该节点不可达），则证明该对象是不可用的。

![reference chain](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_gc_reference_chain.png "reference chain")

通过可达性算法，成功解决了引用计数所无法解决的问题“循环依赖”，只要你无法与 GC Root 建立直接或间接的连接，系统就会判定你为可回收对象。那这样就引申出了另一个问题，哪些属于 GC Root。

在 Java 语言中，可作为 GC Root 的对象包括以下4种：

- 虚拟机栈（栈帧中的本地变量表）中引用的对象，本地变量
- 方法区中类静态属性引用的对象，静态变量
- 方法区中常量引用的对象，final修饰的常量
- 本地方法栈中 JNI（即一般说的 Native 方法）引用的对象

![jvm_memory_area](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/jvm_memory_area.png "jvm_memory_area")

**虚拟机栈（栈帧中的本地变量表）中引用的对象**

此时的 s，即为 GC Root，当s置空时，String 对象也断掉了与 GC Root 的引用链，将被回收。

```java
public static void testGC() {
    String s = new String("localParameter");
    s = null;
}
```

**方法区中类静态属性引用的对象**

s 为 GC Root，s 置为 null，经过 GC 后，s 所指向的 properties 对象由于无法与 GC Root 建立关系被回收。

而 m 作为类的静态属性，也属于 GC Root，parameter 对象依然与 GC root 建立着连接，所以此时 parameter 对象并不会被回收。

静态变量依然可以通过类访问到，如`MethodAreaStaicProperties.m`

```java
public class MethodAreaStaicProperties {
    public static MethodAreaStaicProperties m;
    public MethodAreaStaicProperties(String name){}
}

public static void testGC() {
    MethodAreaStaicProperties s = new MethodAreaStaicProperties("properties");
    s.m = new MethodAreaStaicProperties("parameter");
    s = null;
}
```

**方法区中常量引用的对象**

m 即为方法区中的常量引用，也为 GC Root，s 置为 null 后，final 对象也不会因没有与 GC Root 建立联系而被回收。PS，这是静态常量，那么如果是非静态的呢？

```java
public class MethodAreaStaicProperties {
    public static final MethodAreaStaicProperties m = MethodAreaStaicProperties("final");
    public MethodAreaStaicProperties(String name){}
}

public static void testGC(){
    MethodAreaStaicProperties s = new MethodAreaStaicProperties("staticProperties");
    s = null;
}
```

**本地方法栈中引用的对象**

任何 Native 接口都会使用某种本地方法栈。实现的本地方法接口是使用 C 连接模型的话，那么它的本地方法栈就是 C 栈。

当线程调用 Java 方法时，虚拟机会创建一个新的栈帧并压入 Java 栈。

然而当它调用的是本地方法时，虚拟机会保持 Java 栈不变，不再在线程的 Java 栈中压入新的帧，虚拟机只是简单地动态连接并直接调用指定的本地方法。

## 怎么回收垃圾

在确定了哪些垃圾可以被回收后，垃圾收集器要做的事情就是开始进行垃圾回收，但是这里面涉及到一个问题是：如何高效地进行垃圾回收。由于Java虚拟机规范并没有对如何实现垃圾收集器做出明确的规定，因此各个厂商的虚拟机可以采用不同的方式来实现垃圾收集器，这里我们讨论几种常见的垃圾收集算法的核心思想。

### 标记清除算法

![java_gc_mark_sweep](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_gc_mark_sweep.png "java_gc_mark_sweep")

标记清除算法（Mark-Sweep）是最基础的一种垃圾回收算法，它分为两个阶段：标记阶段、清除阶段。

标记阶段会通过可达性分析将不可达的对象标记出来。清除阶段会将标记阶段标记的垃圾对象清除。

这逻辑再清晰不过了，并且也很好操作，但它存在一个很大的问题，那就是内存碎片。

### 标记压缩算法

标记压缩算法可以解决标记清除算法的内存碎片问题。在清除垃圾对象后增加了一步，内存碎片整理。

把存活的对象移到堆空间的前面部分以保持已使用的堆空间的连续性。

### 复制算法

![java_gc_copying](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_gc_copying.png "java_gc_copying")

复制算法（Copying）是在标记清除算法上演化而来，解决标记清除算法的内存碎片问题。它将可用内存按容量划分为大小相等的两块，每次只使用其中的一块。当这一块的内存用完了，就将还存活着的对象复制到另外一块上面，然后再把已使用过的内存空间一次清理掉。保证了内存的连续可用，内存分配时也就不用考虑内存碎片等复杂情况，逻辑清晰，该算法在存货对象少，垃圾对象多的情况下，非常高效。

复制过一次之后，使用的是包含存活对象的空间。满了之后向空闲的原空间复制。

问题是浪费了一半的空间。

### 标记整理算法

![java_gc_mark_compact](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_gc_mark_compact.png "java_gc_mark_compact")

标记整理算法（Mark-Compact）标记过程仍然与标记清除算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向一端移动，再清理掉端边界以外的内存区域。PS，这里可以说的更清楚些。

标记整理算法一方面在标记-清除算法上做了升级，解决了内存碎片的问题，也规避了复制算法只能利用一半内存区域的弊端。看起来很美好，但从上图可以看到，它对内存变动更频繁，需要整理所有存活对象的引用地址，在效率上要差很多。

### 分代收集算法

分代收集算法（Generational Collection）严格来说并不是一种思想或理论，而是融合上述3种基础的算法思想，而产生的针对不同情况所采用不同算法的一套组合拳。

根据对象存活周期的不同，将内存划分为几块。一般是把 Java 堆分为新生代和老年代，这样就可以根据各个年代的特点采用最适当的收集算法。

在新生代中，每次垃圾收集时都发现有大批对象死去，只有少量存活，那就选用复制算法，只需要付出少量存活对象的复制成本就可以完成收集。

而老年代中因为对象存活率高、没有额外空间对它进行分配担保，就必须使用标记清除或者标记整理算法来进行回收。

## JVM内存模型和分代GC

![java_gc_heap_generation](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_gc_heap_generation.png "java_gc_heap_generation")

Java 堆主要分为2个区域-年轻代与老年代，其中年轻代又分 Eden 区和 Survivor 区，其中 Survivor 区又分 From 和 To 2个区。

### Eden 区

IBM 公司的专业研究表明，有将近98%的对象是朝生夕死，所以针对这一现状，大多数情况下，对象会在新生代 Eden 区中进行分配，当 Eden 区没有足够空间进行分配时，虚拟机会发起一次 Minor GC，Minor GC 相比 Major GC 更频繁，回收速度也更快。
**通过 Minor GC 之后**，Eden 会被**清空**，Eden 区中绝大部分对象会被回收，而那些无需回收的存活对象，将会**进到 Survivor 的 From 区**（**若 From 区不够，则直接进入 Old 区**）。

### Survivor 区

Survivor 区相当于是 Eden 区和 Old 区的一个缓冲，确保进入 Old 区的对象是真正长期存活的。Survivor 又分为2个区，一个是 From 区，一个是 To 区。每次执行 Minor GC，会将 Eden 区和 From 存活的对象放到 Survivor 的 To 区（如果 To 区不够，则直接进入 Old 区）。第二次 Minor GC 时，From 与 To 职责兑换，这时候会将 Eden 区和 To 区中的存活对象再复制到 From 区域，以此反复。

设置两个 Survivor 区最大的好处就是可以使用复制算法解决内存碎片化。

如果只有一个 Survivor 区，在 Minor GC 执行后，Eden 区被清空了，存活的对象放到了 Survivor 区，而之前 Survivor 区中的对象，可能也有一些是需要被清除的。问题来了，这时候我们怎么清除它们？在这种场景下，我们只能标记清除，从而会导致内存碎片化。

如果更多个 Survivor 区，每块空间都会比较小，容易满。

### Old 区
老年代占据着2/3的堆内存空间，只有在 Major GC 的时候才会进行清理，每次 GC 都会触发“Stop-The-World”。内存越大，STW 的时间也越长。采用的是标记整理算法。

在内存担保机制下，无法安置的对象会直接进到老年代，以下几种情况也会进入老年代。

大对象。
大对象指需要大量连续内存空间的对象，这部分对象不管是不是“朝生夕死”，都会直接进到老年代。这样做主要是为了避免在 Eden 区及2个 Survivor 区之间发生大量的内存复制。当你的系统有非常多“朝生夕死”的大对象时，得注意了。

长期存活对象
虚拟机给每个对象定义了一个对象年龄（Age）计数器。正常情况下对象会不断的在 Survivor 的 From 区与 To 区之间移动，对象在 Survivor 区中每经历一次 Minor GC，年龄就增加1岁。当年龄增加到15岁时，这时候就会被转移到老年代。当然，这里的15，JVM 也支持进行特殊设置。

动态对象年龄
虚拟机并不重视要求对象年龄必须到15岁，才会放入老年区，如果 Survivor 空间中相同年龄所有对象大小的总合大于 Survivor 空间的一半，年龄大于等于该年龄的对象就可以直接进去老年区，无需等你“成年”。

### Minor GC

在进行Minor GC之前，JVM还有一步操作，他会检查新生代所有对象使用的总内存是否小于老年代最大剩余连续内存，如果上述条件成立，那么这次Minor GC一定是安全的，因为即使所有新生代对象都进入老年代，老年代也不会内存溢出。

如果上述条件不成立，JVM会查看参数 HandlePromotionFailure 是否开启（JDK1.6以后默认开启），如果没开启，说明Minor GC后可能会存在老年代内存溢出的风险，会进行一次Full GC，如果开启，JVM还会检查历次晋升老年代对象的平均大小是否小于老年代最大连续内存空间，如果成立，会尝试直接进行Minor GC，如果不成立，老年代执行Full GC。

Minor GC的问题在于，新生代的对象可能被老年代引用，而这种情况可达性分析是分析不到的，但这种情况的新生代对象是不应该被回收的。

HotSpot虚拟机提供了一个解决方案：卡表。这种方法会将老年代内存平均分为很多的卡片，每个卡片都包含一部分对象，然后维护一个卡表，卡表是一个数组，每个元素指向一个卡片，并标识出这个卡片中有没有指向新生代的对象，如果有标识为1。这样一来，Minor GC只需要扫描卡表中标识为1的卡片即可，大大提升了效率。

![java_gc_minor_old_link_young](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_gc_minor_old_link_young.png "java_gc_minor_old_link_young")

### Major GC / Full GC



## GC类型

根据GC算法的不同其执行过程也会有所区别。连线的部分标识可以配合使用。

![java_gc_categories](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_gc_categories.png "java_gc_categories")

对应的JVM参数如下，PS，不确定是否正确，自己写程序验证GC类型，Java8：

| 新生代(别名)  |  老年代 | JVM 参数  | First | Second |
| :------------: | :------------: | :------------: | :------------: | :------------: |
| Serial (DefNew)  | Serial Old(PSOldGen)  | -XX:+UseSerialGC  | Copy | MarkSweepCompact |
| Parallel Scavenge (PSYoungGen)  |  Serial Old(PSOldGen) |  -XX:+UseParallelGC | PS Scavenge | PS MarkSweep |
| Parallel Scavenge (PSYoungGen) | Parallel Old (ParOldGen)  | -XX:+UseParallelOldGC  | PS Scavenge | PS MarkSweep |
| ParNew (ParNew)  | Serial Old(PSOldGen)  | -XX:+UseParNewGC  | ParNew | MarkSweepCompact |
| ParNew (ParNew)  | CMS+Serial Old(PSOldGen)  |-XX:+UseConcMarkSweepGC   | ParNew | ConcurrentMarkSweep |
| G1   | G1  |  -XX:+UseG1GC | G1 Young Generation | G1 Old Generation |

**PS Scavenge 即 Copy**
**PS MarkSweep 即 MarkSweepCompact**

下面介绍每种GC的特性：

### Serial GC(-XX:+UseSerialGC)

使用单一线程执行GC，在新生代使用复制算法，在老年代使用标记压缩算法（mark-sweep-compact）

Serial GC适用于CPU核数较少且使用的内存空间较小的场景。

### Parallel GC(-XX:+UseParallelGC)

GC算法与 Serial GC 相同，但使用多个线程并发执行，因此 parallel GC 具有更快的速度。又被称为"高吞吐GC(throughput GC)"

适用于多核CPU且使用了较大内存空间的场景。

### Parallel Old GC(-XX:+UseParallelOldGC)

与Parallel GC相比唯一的区别在于Parallel的GC算法是为老年代设计的。它的执行过程分为三步：标记(mark)--总结(summary)--压缩(compaction)。其中summary步骤会分别为存活的对象在已执行过GC的空间上标出位置，因此与mark-sweep-compact算法中的sweep步骤有所区别，并需要一些复杂步骤才能完成。

PS，不知道这个怎么开启，怎么用

### CMS GC(-XX:+UseConcMarkSweepGC)

![java_gc_cms](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/java_gc_cms.png "java_gc_cms")

并发标记清理，开始时的初始标记(initial mark)比较简单，只有靠近类加载器的存活对象会被标记，因此停顿时间(stop-the-world)比较短暂。在并发标记(concurrent mark)阶段，由刚被确认和标记过的存活对象所关联的对象将被会跟踪和检测存活状态。此步骤的不同之处在于有多个线程并行处理此过程。在重标记(remark)阶段，由并发标记所关联的新增或中止的对象瘵被会检测。在最后的并发清理(concurrent sweep)阶段，垃圾回收过程被真正执行。在垃圾回收执行过程中，其他线程依然在执行。得益于CMS GC的执行方式，在GC期间系统中断时间非常短暂。CMS GC也被称为低延迟GC，适用于所有应用对响应时间要求比较严格的场景。

CMS GC虽然具有中断时间短的优势，其缺点也比较明显：

- 与其他GC相比，CMS GC要求更多的内存空间和CPU资源
- CMS GC默认不提供内存压缩，为了避免过多的内存碎片而需要执行额外的压缩任务时，CMS GC会比任何其他GC带来更多的stop-the-world时间

### G1 GC

先忘记上面介绍的有关新生代和老年代的知识。如上图所示，每个对象在创建时会分析到一个格子中，后续的GC也是在格子中完成的。每当一个区域分配满对象后，新创建的对象就会分配到另外一个区域，并开始执行GC。在这种GC中不会出现其他GC中的对象在新生代和老生代三区域中移动的现象。G1是为了取代在长期使用中暴露出大量问题且饱受抱怨的CMS GC。

G1最大的改进在于其性能表现，它比以上任何一种GC都更快速。