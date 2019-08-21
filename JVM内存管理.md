# JVM 内存模型

Java虚拟机在执行Java程序的过程中会把他所管理的内存划分为若干个不同的数据区域。

主要包含了程序计数器（PC寄存器）、Java虚拟机栈、本地方法栈、Java堆、方法区以及运行时常量池（在方法区中）。

但是，需要注意的是，上面的区域划分只是**逻辑区域**，对于有些区域的限制是比较松的，所以不同的虚拟机厂商在实现上，甚至是同一款虚拟机的不同版本也是不尽相同的。

![jvm_memory_area](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/jvm_memory_area.png "jvm_memory_area")

## Java8中的方法区、永久代、Metaspace

在最著名的HotSopt虚拟机实现中（在Java 8 之前），方法区仅是逻辑上的独立区域，在物理上并没有独立于堆而存在，而是位于永久代中。所以，这时候方法区也是可以被垃圾回收的。

在Java 8中 ，HotSpot虚拟机移除了永久代，使用本地内存来存储类元数据信息并称之为：元空间（Metaspace）

![jvm_memory_remove_perm](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/jvm_memory_remove_perm.png "jvm_memory_remove_perm")

## 堆和栈的区别是什么？

堆和栈（虚拟机栈）是完全不同的两块内存区域，一个是线程独享的，一个是线程共享的，二者之间最大的区别就是存储的内容不同：

堆中主要存放对象实例。

栈（局部变量表）中主要存放各种基本数据类型、对象的引用。

## Java中的数组是存储在堆上还是栈上的？

在Java中，数组同样是一个对象，所以对象在内存中如何存放同样适用于数组；

所以，数组的实例是保存在堆中，而数组的引用是保存在栈上的。

## Java中对象创建的过程是怎么样的？

对于一个普通的Java对象的创建，大致过程如下：

1. 虚拟机遇到new指令，到常量池定位到这个类的符号引用。
2. 检查符号引用代表的类是否被加载、解析、初始化过。
3. 虚拟机为对象分配内存。
4. 虚拟机将分配到的内存空间都初始化为零值。
5. 虚拟机对对象进行必要的设置。
6. 执行方法，成员变量进行初始化。

## Java中的对象一定在堆上分配内存吗？

Java堆中主要保存了对象实例，但是，随着JIT编译期的发展与逃逸分析技术逐渐成熟，栈上分配、标量替换优化技术将会导致一些微妙的变化，所有的对象都分配到堆上也渐渐变得不那么“绝对”了。

其实，在编译期间，JIT会对代码做很多优化。其中有一部分优化的目的就是减少内存堆分配压力，其中一种重要的技术叫做逃逸分析。

如果JIT经过逃逸分析，发现有些对象没有逃逸出方法，那么有可能堆内存分配会被优化成栈内存分配。

![jvm_memory_object_allocate](https://raw.githubusercontent.com/yetao93/JavaNote/master/md_pic/jvm_memory_object_allocate.png "jvm_memory_object_allocate")

## 如何获取堆和栈的dump文件？

Java Dump，Java虚拟机的运行时快照。将Java虚拟机运行时的状态和信息保存到文件。

可以使用在服务器上使用jmap命令来获取堆dump，使用jstack命令来获取线程的调用栈dump。

