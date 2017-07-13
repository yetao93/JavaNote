# CRUD

## save
- **URL**: /rest/dc/<dataset-name\>/<entity-name\>
- **METHOD**: HTTP POST
- **REQUEST BODY**: JSON 表示的对象，后台接收到 Map<String, Object>
- **RESPONSE BODY**: 如果成功，返回 JSON 对象  { "id"=id } ，表示存储的对象的ID。

### 过程：

1. 通过实体类的名称，获得类全名，通过反射创建bean对象
2. **获得包括父类在内的所有属性**
3. **将map转换为bean**，将属性名作为key从map中得到value设置进对象中
4. baseDao保存bean对象，返回id

*不能级联新增*

## list
- **URL**: /rest/dc/<dataset-name\>/<entity-name\>[?params]
- **METHOD**: HTTP GET
- **PARAMS**: first,limit,orderBy,orderDir,expands
- **RESPONSE BODY**: 如果成功，返回 JSON 数组  [{object1}, {object2}...]

### 参数含义：

**first**：数值型，表示从第几项开始返回结果；

**limit**：数值型，表示最多返回多少项结果；

**orderBy**：排序字段，必须是实体类的某个字段的名称，不支持多字段排序；

**orderDir**: 排序方向，必须为 asc或desc之一；

**countOnly**: 当countOnly为true时，返回实体对象的数量，其他参数会忽略；

**expands**: 返回关联对象的属性;

### 过程：
1. 格式化的hql语句，`String hql = String.format("from %s e", entityName);`
2. 遍历类的所有属性，将用于排序的参数拼接上
3. 执行查询，得到`org.hibernate.query.Query`，通过`setFirstResult`和`setMaxResults`来分页，得到List<Bean\>
4. 遍历类的所有属性，**根据expands参数加载对象中对应的属性，同时将bean转换为map**
5. 返回List<Map\>

## get
- **URL:** /rest/dc/<dataset-name\>/<entity-name\>/<id\>
- **METHOD:** HTTP GET
- **PARAMS:** expands
- **RESPONSE BODY:** 如果成功，返回 JSON 对象。

### 过程：
1. 通过baseDao根据id和entityName获得对象
2. 同上个list()方法一样，根据expands参数加载对象中的属性，并将bean转换为map返回

## update
- **URL:** /rest/dc/<dataset-name\>/<entity-name\>/<id\>
- **METHOD:** HTTP POST
- **REQUEST BODY:** JSON 格式表示的对象内容
- **RESPONSE BODY:** 无

### 过程：
1. 通过baseDao根据id和entityName获得对象
2. 遍历类的所有属性，根据请求体解析的map判断，如果map中包含类的属性，则将其设置为新值
3. 如果要改变对象的关联关系，直接设置新的关联属性的对象的ID
3. 在实体类未脱离session时，设置新值后会自动保存
4. 注意关联关系由哪一端掌控，比如修改Customer中的orders属性无效，修改Order中的customer属性有效

*可以局部修改*

## remove
- **URL:** /rest/dc/<dataset-name\>/<entity-name\>/<id\>
- **METHOD:** HTTP DELETE
- **REQUEST BODY:** 无
- **RESPONSE BODY:** 无

### 过程：
即调用Hibernate session的delete方法，删除持久化对象，注意级联问题，很有可能删不掉

# QUERY
**参数params不能为null**
## restQuery（返回类型只会是List<Map\>）
`public List restQuery(String queryName, Map<String, Object> params, Integer first, Integer limit)`

供前端直接调用yaml里定义好的查询，需要resultType、fetches（restQuery，hql并且返回类型为domain）

返回类型resultType从yaml中获取，进行判断：一般是domain类，如果是map或非domain类，则需在hql语句中写好`new map(...)`或`new Object(...)`

处理hql语句，调用`org.hibernate.Session.createQuery(String hql, Class<T> resultType)`，转换为map，若返回类型为domain，根据fetches加载关联属性，将domain转换为map返回

处理sql语句，则调用`org.hibernate.Session.createNativeQuery(String sqlString)`，得到List<Map\>，用不到yaml中的其他属性



## query（返回类型根据方法的参数`Class<R> resultType`和语法来定）
`public <R> List<R> query(String queryName, Map<String, Object> params, Class<R> resultType, Integer first, Integer limit)`

若resultType为map，调用上面的restQuery()方法，返回的一定是List<Map\>

若resultType为domain，不需要resultType和fetches，继续该方法：语法为sql，返回的是List<Map\>，那么此分支可以作废，功能跟restQuery重复

语法为hql，返回的是List<domain\>，**直接给前端返回会导致延迟加载错误！**

## 分页
参数first，首位从0开始，等于null时转化为0，为负数时倒过来查

参数limit，为0或null时查全部，等于null时不设置最大数量

## YAML

### if的用法

`#if($id)`判断params.get("id")是否不等于null，null不能通过，空字符串能通过

`#if($id!='')`判断params.get("id")是否不等于空字符串，空字符串不能通过，null能通过

`#if($id!=null)`报错

**综上，缺少一个同时判断不为null并且不为空的方法**

### in的用法
	- name: testIn
      resultType: Customer
      statement: '
        select 
          c 
        from 
          Customer c
        where
          c.id #qin($ids "IDS")
          and c.name #qin($names "N")'

params如果不传这些参数或者传的是空数组则查询结果为空

### like的用法
可以用#qlikeany来写参数数目不定的 like 表达式。例如：

	---
	    - name: testIn
	      resultType: Customer
	      statement: '
	      select 
	           c 
	         from 
	           Customer c
	         where
	             #qlikeany("c.name" $names)
	      '
其中 "c.name" 是字段名，$names 是数组形式传入的参数。