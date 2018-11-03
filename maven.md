# Maven

## 依赖

典型：

	<dependency>
		<groupId>
		<artifactId>
		<version>
		<type>
		<scope>
		<optional>
		

### 依赖范围 scope

Maven在编译、测试、运行项目的时候，均需要classpath，并且三种classpath可以不同

依赖范围就是用来控制依赖与这三种classpath的关系。

- compile：默认值，对于编译、测试、运行三种classpath都有效。比如spring-core，一直都需要。

- test：对测试classpath有效，在编译代码或运行时均无法使用该依赖。比如JUnit，只在编译测试代码和运行测试时才需要。

- provided：对编译和测试classpath有效，运行时无效。比如servlet-api，在运行时由容器提供。

- runtime：对测试和运行时有效，编译时无效。比如mysql-jdbc，项目编译时使用JDK的jdbc接口，只有测试和运行时需要实现类

- system：跟provided相同，但是使用时必须通过systemPath元素显式制定依赖文件的路径，谨慎使用。

- import: 对依赖范围无影响，将目标POM中的dependencyManagement导入合并到当前POM


### 依赖调解

1. 项目有这样的依赖关系：A->B->C->X(1.0)、A->D->X(2.0).

X是A的传递性依赖，但是两条依赖路径上有两个版本的X，为了避免依赖重复，Maven依赖调解的第一原则是：路径最短者优先。该例中X(2.0)路径短，因此会使用X(2.0)

2. 项目有这样的依赖关系：A->B->X(1.0)、A->D->X(2.0).

如果依赖路径长度一样，Maven定义了依赖调解的第二原则：先声明者优先。在依赖路径相等的前提下，在POM中依赖声明的顺序决定了谁会被解析使用，顺序最靠前的那个依赖优胜。

### 可选依赖 optional

	<optional>true</optional>

只会对当前项目产生影响，当其他项目依赖于当前项目时，这个依赖不会被传递。如需使用，要在其他项目里显式声明。


### 排除传递性依赖 exclusions

	<exclusions></exclusions>
