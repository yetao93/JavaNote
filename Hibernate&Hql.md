# Hibernate

**Hibernate的作用：**目前主流的数据库是关系数据库，而Java语言则是面向对象的编程语言，二者结合相当麻烦，Hibernate减少了这个问题的困扰，它使得开发者可以完全采用面向对象的方式来开发应用程序。Hibernate相当于桥梁，允许程序员采用面向对象的方式来操作关系数据库。

---

**ORM（Object/Relation Mapping）对象/关系数据库映射**，其中非常重要的一个媒介 —— PO

**PO （Persistent Object） 持久化对象**，完成持久化操作，通过该对象可对数据执行增、删、改，是以面向对象的方式操作数据库应用程序与数据库没有直接联系，只需创建、修改、删除持久化对象即可，与此同时，Hibernate负责把这种操作转换为对数据表的操作

**POJO （普通、传统的Java对象，类似JavaBean）持久化类**，Hibernate被称为低侵入式设计的原因是其直接采用了POJO作为PO，不继承父类不实现接口不被污染

普通的JavaBean还不具备持久化操作的能力，所以要添加一些注解或经过XML文件配置。

**PO = POJO + 持久化注解**

在JavaBean里添加注解，修改hibernate.cfg.xml里的< mapping class="包名+类名">，不用加.java

完成消息插入的工作，一个java类，PO只有在Session的管理下才可完成数据库的访问。步骤：

1.开发持久化类domain

2.获取Configuration 

3.获取SessionFactory 

4.获取Session 打开事务 

5.用面向对象的方式操作数据库

6.关闭事务，关闭Session


###关于实体类domain

1. 持久化类的类名不能重复！不同包也不行 

2. Hibernate 要求声明集合属性只能用 Set、List、Map、SortedSet、SortedMap 等接口，而不能用 HashSet、ArrayList、HashMap、TreeSet、TreeMap 等实现类。其原因就是因为 Hibernate 需要对集合属性进行延迟加载，而 Hibernate 的延迟加载是依靠 PersistentSet、PersistentList、PersistentMap、PersistentSortedMap、PersistentSortedSet 这些Hibernate提供的实现类来完成的。不过 PersistentSet 等集合里持有一个 session 属性，这个 session 属性就是 Hibernate Session，当程序需要访问 PersistentSet 集合元素时，PersistentSet 就会利用这个 session 属性去抓取实际的对象对应的数据记录。





##在Hibernate中使用hql语句查询：

####查询整个实体类，得到List&lt;domain&gt;
	String hql = "from Users";
    Query query = session.createQuery(hql);   
	List<Users> users = query.list();   　

####查询其中几个字段，得到List&lt;Object[]&gt;   
        String hql = " select name,passwd from Users";   
        Query query = session.createQuery(hql);  
        List<Object[]> list = query.list();   

####修改默认查询结果query.list()不以Object[]数组形式返回，以List形式返回
	String hql = " select new list(name,passwd) from Users";   
	Query query = session.createQuery(hql);   
    List<List> list = query.list();  

####修改默认查询结果(query.list())不以Object[]数组形式返回，以Map形式返回，带有字段名
	String hql = " select new map(name as username,passwd as password) from Users";
	Query query = session.createQuery(hql);   
    List<Map<String,Object>> list = query.list();  

####执行更新、删除操作，要加executeUpdate()
	String hql = "delete from Officer where id =1";
	Query query = session.createQuery(hql);
	query.executeUpdate();

## 在Hibernate中使用sql语句来查询，得到结果：

    String sql = "select * from user";
    SQLQuery query = session.createSQLQuery(sql);
    List list = query.list();
    for(int i = 0; i < list.size(); i++){
        Object[] objects = (Object[]) list.get(i);
        System.out.println(Arrays.toString(objects));
	}
    
返回的结果是SQLQuery类型，通过其`list()`方法可转换为List类型，此时List的泛型应为Object[]数组，即使查询一个字段也是数组形式返回，将List每个元素强制转换为Object[]数组，即可用，例如`[1, John, bbb, admin]`

也可将List的泛型改为`Map<String,Object>`类型，增加这句

    query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
    List<Map<String, Object>> list = query.list();
    
得到的结果带字段名`[{USER_ID=1, PASSWORD=bbb, TYPE=admin, NAME=John}]`


### 占位符和设置参数
所有可能的占位符如下：

    String sql1 = "select * from user where USER_ID = ?";
	String sql2 = "select * from user where USER_ID = :id";
	String sql3 = "select * from user where USER_ID = ?1";//?后面只能为数字
	String sql4 = "select * from user where USER_ID = ?id";//这句sql是错的，不可用
    
Query有setParameter和setXxx两种设置参数方式，setXxx就是按不同的数据类型对setParameter的细化，用setParameter即可。

又有两种：setParameter(String,Object)和setParameter(Integer,Object)

对于sql1，用setParameter(Integer,Object)，按顺序从0开始

sql3是对sql1的自定义排序，以？后面的数字为索引setParameter(Integer,Object)

sql2用setParameter(String,Object)，索引为：后面的字符串

###创建UUID

	@Id
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@GeneratedValue(generator = "UUID")
	String id; 

###缓存
**Hibernate一级缓存：**

Session 级别的缓存，它同 session邦定。它的生命周期和 session相同。 Session消毁，它也同时消毁；管理一级缓存，一级缓存无法取消。

**Hiberante二级缓存：**

1． Hibernate3的（ sessionFactory）二级缓存和 session级别的缓存一样都只对实体对象做缓存，不对属性级别的查询做缓存；二级缓存的生命周期和 sessionFactory的生命周期是一样的， sessionFactory可以管理二级缓存；  

2． sessionFactory级别的缓存，需要手动配置；所有的 session可以共享 sessionFactory 级别的缓存；（一般把一些不经常变化的实体对象放到 sessionFactory级别的缓存中，适合放不经常变化的实体对象。）  

##关联关系

- 可分为有关联表和无关联表
- 主表：主键是其他表的外键
- 从表：依赖于主表，以主表的主键为外键，通过外键与主表关联查询

###单向N-1（多个人住一个地址）
原理：在N的一端的持久化类中增加一个属性，该属性引用1的一端的关联实体，因为非集合属性，就相当于unique=true

操作：基于外键，**N端**@ManyToOne @JoinColumn ； **1端**无变化

###单向1-1（一个人有一个手机）
原理：跟单向N-1一样，在掌控关系的1端增加代表关联实体的属性

操作：基于外键，**掌控关系的1端**@OneToOne @JoinColumn(unique=true) ； **1端**无变化

###单向1-N（一个富人有多套房子）
原理：在1的一端使用集合属性，元素是N端的关联实体

操作1：基于外键，**但是此时的外键列添加到N端实体对应的表中**，**1端**@OneToMany @JoinColumn ； **N端**无变化

缺点：因为外键列在N端的表中，而且新增时先保存N端实体，Hibernate会先用一条insert语句插入N端实体的记录，外键为null，再用一条update语句修改，设置该记录的外键为1端的主键。**性能不好，建议改为使用双向1-N关联，或者用连接表**

操作2：基于连接表，**1端**@OneToMany @JoinTable（N端的外键列unique=true） ； **N端**无变化

###单向N-N（家人有多辆车）
原理：跟单向1-N相同，在掌控关系的N端使用集合属性，元素是另一个N端的关联实体，区别就是没有unique=true

操作：基于连接表，**掌控关系的N端**@ManyToMany @JoinTable ； **不掌控关系的N端**无变化

###双向N-1（多个学生对应一个老师）
原理：使用N端控制关联关系，两端都增加对关联关系的访问，N端增加关联实体的属性，1端增加集合属性，元素为关联实体。数据库只是在N端的表中增加一个外键列指向1端的主键

操作：基于外键，**N端**@ManyToOne  @JoinColumn，**1端**@OneToMany(mappedBy=)

注意：最好先持久化1端实体，在N端给关联属性设置值，再持久化N端实体。不要通过1端的集合属性来设置，因为1端不控制关联关系

###双向1-1（一个丈夫对应一个妻子）
原理：外键可以存放在任意一端，使用unique=true

操作：两端都使用@OneToOne。在存放外键的一端使用@JoinColumn映射外键列，增加unique=true，另一端增加mappedBy属性

###双向N-N（多个员工对应多个客户）
原理：两端都设置集合属性

操作：基于连接表，在两端都使用 @ManyToMany @JoinTable 显式映射连接表，指定的表名要相同，指定的外键列相互对应。如果想要末端放弃控制关联关系，在@ManyToMany中指定mappedBy属性，并去掉@JoinTable即可

###注解的含义

####@ManyToOne、@OneToMany、@OneToOne、@ManyToMany

修饰代表关联实体的属性，To之前的代表本体，To之后代表关联体，若为Many则应该是集合，泛型为关联体。

**targetEntity**，能通过反射来确定关联实体的类型，但是如果是集合并且不带泛型时，就必须指定

**cascade**，默认不启用任何级联。对关联实体采取的级联策略，可组合，可用Hibernate的注解@Cascade代替，后面有详解

**fetch**，抓取关联实体的策略，对One端默认是Eager，对Many端默认Lazy，在之后SSH结合时再多做测试。可通过**Hibernate.initialize(list)** 强制读取数据，对数据继续采取其本身的抓取策略

**mappedBy**，表示当前实体不控制关联关系，指向关联实体内自己的属性名，不能再用@JoinColumn、@JoinTable

####@JoinColumn   
映射外键列，成为从表

**name**，字段名，默认关联表的名称_关联表主键的字段名 

**referencedColumnName**，关联的字段名，默认关联表的主键的字段名

**unique**，默认false，可以出现多次，有多个记录可以与之关联，关联实体为N；设置为true，则关联实体为1

####@JoinTable

**name**,连接表表名

**joinColumns**，=@JoinColumn，该外键列参照当前实体对应表的主键列

**inverseJoinColumns**,=@JoinColumn，该外键列参照当前实体的关联实体的对应表的主键列

###关于级联
**级联操作一般来说应该是由主表记录传播到从表记录。最好不要对从表加级联操作，尤其是删除，否则一删从表的记录，主表记录也被删除了。**

对于连接表，不管有没有级联，都会对应操作（删除）

级联的意思应该是对相关联的实体进行的操作。没考虑清楚前就不要设置了，或者只设置个级联保存

测试`javax.persistence.CascadeType`的时候有问题，只有设置为ALL才能级联，其他的都不行，为什么?

改为使用`org.hibernate.annotations.Cascade`和`org.hibernate.annotations.CascadeType`。

基于外键关联的，对从表设置为SAVE_UPDATE，取得较为理想的效果，从表记录保存时，会级联保存主表记录。对主表设置DELETE，删除主表记录时，会级联删除从表的记录（若有必要）。

基于连接表的，对一个关联实体设置SAVE_UPDATE，可级联保存另一个关联实体。设置DELETE，删除该关联实体记录时，**会同时删除连接表的有关记录和另一个关联实体的有关记录！慎用！**

####总结，一般来说，对控制关联关系的一端设置Hibernate自带的cascade注解为SAVE_UPDATE取得最好效果。

##Hibernate的Session

实例化的Session是一个**轻量级**的类，创建和销毁它都不会占用很多资源。这在实际项目中确实很重要，因为在客户程序中，可能会不断地创建以及销毁Session对象，如果Session的开销太大，会给系统带来不良影响。但值得注意的是 Session对象是**非线程安全**的，因此在你的设计中，最好是一个线程只创建一个Session对象。   

Hibernate 自身提供了三种管理 Session 对象的方法，在 Hibernate 的配置文件中, hibernate.current_session_context_class 属性用于指定 Session 管理方式

- Session 对象的生命周期与本地线程绑定（thread）
- Session 对象的生命周期与 JTA 事务绑定 （jta）
- Hibernate 委托程序管理 Session 对象的生命周期（managed）

Hibernate 按以下规则把 Session 与本地线程绑定

1. 当一个线程(threadA)第一次调用 SessionFactory 对象的 getCurrentSession() 方法时, 该方法会创建一个新的 Session(sessionA) 对象, 把该对象与 threadA 绑定, 并将 sessionA 返回 

2. 当 threadA 再次调用 SessionFactory 对象的 getCurrentSession() 方法时, 该方法将返回 sessionA 对象 

3. 当 threadA 提交 sessionA 对象关联的事务时, Hibernate 会自动flush sessionA 对象的缓存, 然后**提交事务, 关闭 sessionA 对象**. 当 threadA 撤销 sessionA 对象关联的事务时, 也会自动关闭 sessionA 对象 

4. 若 threadA 再次调用 SessionFactory 对象的 getCurrentSession() 方法时, 该方法会又创建一个新的 Session(sessionB) 对象, 把该对象与 threadA 绑定, 并将 sessionB 返回

**若 Session 是由 thread 来管理的, 则在提交或回滚事务时, 已经关闭 Session 了.**

##Hibernate的SessionFactory

这里用到了一个设计模式――工厂模式，用户程序从工厂类SessionFactory中取得Session的实例。   
    
SessionFactory是**重量级**的**线程安全**的！实际上它的设计者的意图是让它能在整个应用中共享。典型地来说，**一个项目通常只需要一个SessionFactory就够了，但是当你的项目要操作多个数据库时，那你必须为每个数据库指定一个SessionFactory**。   
SessionFactory在Hibernate中实际起到了一个缓冲区的作用，它缓冲了Hibernate自动生成的SQL语句和一些其它的映射数据，还缓冲了一些将来有可能重复利用的数据。

##Hibernate的Configuration
    
Configuration接口的作用是对Hibernate进行配置，以及对它进行启动。在Hibernate的启动过程中，Configuration类的实例首先定位映射文档的位置，读取这些配置，然后创建一个SessionFactory对象。   