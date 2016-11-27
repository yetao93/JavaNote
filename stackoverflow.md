# StackOverFlow

## Q：i += j 等同于 i = i + j 吗？

E1 op= E2 (诸如 `i += j;` `i -= j;` 等等)，其实是等同于 E1 = (T)(E1 op E2)，其中，T是E1这个元素的类型，也就是如果这两个类型不同，是加了强制类型转换的。

---

## Q：将数组转为List

`List<Integer> arrayList = Arrays.asList(1,2,3);`

1.这样做生成的list，是定长的。也就是说，如果你对它做add或者remove，都会抛UnsupportedOperationException。

2.如果修改数组的值，list中的对应值也会改变！

修正这些问题 `ArrayList<Integer> arrayList = new ArrayList<>(Arrays.asList(1,2,3));`
   
   或 `Collections.addAll(Collection<? super Integer> c, Integer... elements);`

---

## Q：Map遍历

`Set<Map.Entry<K,V>> entrySet()` 返回：此映射中包含的映射关系的 set 视图

For-each中，左边是右边集合的一个元素

使用For-Each迭代entries

    for(Map.Entry<Integer, Integer> entry :map.entrySet()){
      System.out.println("key = " + entry.getKey() + ",value = " + entry.getValue());
    }

使用Iterator迭代

    Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    Iterator<Map.Entry<Integer, Integer>> its = map.entrySet().iterator();
    while (its.hasNext()) {
      Map.Entry<Integer, Integer> it = its.next();
      System.out.println("Key = " + it.getKey() + ", Value = " + it.getValue());
    }


---

## Q：如何将String转换为int、long、double、byte[]等

    Integer x = Integer.valueOf(str);//Integer实例

    int y = Integer.parseInt(str);//基本数据类型
    
    byte[] bs = "yetao".getBytes();

---

## Q：如何分割（split）string字符串

使用String的split()方法

如下所示：

    String string = "004-034556";
    String[] parts = string.split("-");
    String part1 = parts[0]; // 004
    String part2 = parts[1]; // 034556

需要注意的是，该方法的参数是个正则表达式,要注意对某些字符做转码。例如，.在正则表达式中表示任意字符，因此，如果你要通过.号做分割，需要这样写，split("\\.")或者split(Pattern.quote("."))

---

## Q：输出 Java 数组最简单的方式

在 Java 5+ 以上中使用 `Arrays.toString(arr)` 或 `Arrays.deepToString(arr)`来打印（输出）数组。

`Arrays.deepToString`与`Arrays.toString`不同之处在于，`Arrays.deepToString`更适合打印多维数组

---

## Q：StringBuilder和StringBuffer有哪些区别呢

最主要的区别，StringBuffer的实现用了synchronized（锁），而StringBuilder没有。

StringBuffer  支持线程同步保证线程安全而导致性能下降 

StringBuilder 非线程安全，在单线程中性能高

---

## Q：try...catch...finally...与return

1.不管有木有出现异常，finally块中代码都会执行；

2.当try和catch中有return时，finally仍然会执行；

3.finally是在return后面的表达式运算后执行的（此时并没有返回运算后的值，而是先把要返回的值保存起来，不管finally中的代码怎么样，返回的值都不会改变，仍然是之前保存的值），所以函数返回值是在finally执行前确定的；

4.finally中最好不要包含return，否则程序会提前退出，返回值不是try或catch中保存的返回值。


---

## Q：




## A：

---

## Q：




## A：

---

## Q：




## A：

---

## Q：




## A：

---

## Q：




## A：

---

## Q：




## A：

---

## Q：




## A：

---

## Q：




## A：

---

## Q：




## A：

---

