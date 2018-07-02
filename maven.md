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

scope有如下几种：compile、provide、runtime、test、system、import

import: 将目标POM中的dependencyManagement导入合并到当前POM

### 传递性依赖和依赖范围



### 依赖调解

1. 项目有这样的依赖关系：A->B->C->X(1.0)、A->D->X(2.0).

X是A的传递性依赖，但是两条依赖路径上有两个版本的X，为了避免依赖重复，Maven依赖调解的第一原则是：路径最短者优先。该例中X(2.0)路径短，因此会使用X(2.0)

2. 项目有这样的依赖关系：A->B->X(1.0)、A->D->X(2.0).

如果依赖路径长度一样，Maven定义了依赖调解的第二原则：先声明者优先。在依赖路径相等的前提下，在POM中依赖声明的顺序决定了谁会被解析使用，顺序最靠前的那个依赖优胜。

### 可选依赖

	<optional>true</optional>

只会对当前项目产生影响，当其他项目依赖于当前项目时，这个依赖不会被传递。如需使用，要在其他项目里显式声明。