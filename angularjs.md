# AngularJS

##简介
AngularJS是一个JavaScript框架，是一个以JavaScript编写的库。它可通过`<script>`标签添加到HTML页面。

AngularJS通过指令扩展了HTML，通过表达式绑定数据到HTML。



##要解决的问题
AngularJS是为了克服HTML在构建应用上的不足而设计的。HTML是一门很好的为静态文本展示设计的声明式语言，但要构建WEB应用的话它就显得乏力了。

通常使用类库（如：jQuery）、框架（如：knockout）来解决静态网页技术在构建动态应用上的不足。

但AngularJS使用了不同的方法，它尝试去补足HTML本身在构建应用方面的缺陷。AngularJS通过使用我们称为标识符(directives)的结构，让浏览器能够识别新的语法。

## 一个实例

    <!DOCTYPE html>
    <html>
    <head>
    <meta charset="utf-8">
    <script src="http://apps.bdimg.com/libs/angular.js/1.4.6/angular.min.js"></script>
    </head>
    <body>

    <div ng-app="">
    <p>名字 : <input type="text" ng-model="name"></p>
    <h1>Hello {{name}}</h1>
    </div>

    </body>
    </html>
当网页加载完毕，AngularJS 自动开启。

ng-app 指令告诉 AngularJS，`<div>` 元素是 AngularJS 应用程序 的"所有者"，ng-app指令标记了AngularJS脚本的作用域。

ng-model 指令可以将输入域的值与 AngularJS 创建的变量绑定。

##AngularJS 指令

_AngularJS 指令是以 ng 作为前缀的 HTML 属性。_

**ng-init** 指令初始化 AngularJS 应用程序变量，不常用，初始化后在js中取不到

**ng-bind** 指令把应用程序变量 name 绑定到某个段落的 innerHTML，相当于{{}}，里面也可以写表达式，如5+5

    <div ng-app="" ng-init="firstName='John'">
      <p>姓名为 <span ng-bind="firstName"></span></p>
    </div> 

**ng-model** 在AngularJS中，只需要使用ng-model指令就可以把应用程序数据与HTML元素绑定，实现model和view的双向绑定。如下所示，input框输入的任何值都会赋给name变量，如果ng-model绑定到div则没什么用，相当于声明了个变量

    请输入任意值：<input type="text" ng-model="name" />
    你输入的为： {{ name }}

**ng-click**  AngularJS也有自己的HTML事件指令,比如说通过ng-click定义一个AngularJS单击事件。对按钮、链接等，我们都可以用ng-click指令属性来实现绑定

    <button ng-click="click= !click">隐藏/显示</button>
    <div ng-hide="click">
        请输入一个名字：<input type="text" ng-model="name" />
        Hello <span ng-bind="name"></span> 
    </div>

**ng-hide、ng-show、ng-if** 用于控制部分HTML元素是否可见

    <button ng-click="click= !click">我变</button>
    <p ng-hide="click">显示了。</p>
    <p ng-hide="!click">隐藏了。</p>

值默认是false，虽然隐藏了但还是会加载，要提升速度就用ng-if如果不显示就不会加载，ng-if里面放表达式的值，无需{{ }}，为false、null、0的时候不显示，其他的如字符串都显示

**ng-repeat** 遍历一个数据集合中的每个数据元素，并且加载HTML模版把数据渲染出来 

	<select ng-model="selectedSite">
	<option ng-repeat="x in sites" value="{{x.url}}">{{x.site}}</option>
	</select>

选择的是字符串，要选择对象用ng-options

**ng-options** 可以选择对象

	<select ng-model="selectedSite" ng-options="x.site for x in sites"></select>

##AngularJS 表达式

AngularJS 表达式写在双大括号内：{{ expression }}。

AngularJS 表达式把数据绑定到 HTML，这与 ng-bind 指令有异曲同工之妙。但不同之处就在于ng-bind是在angular解析渲染完毕后才将数据显示出来的。

使用花括号语法时，因为浏览器需要首先加载页面，渲染它，然后AngularJS才能把它解析成你期望看到的内容，所以对于首个页面中的数据绑定操作，建议采用ng-bind，以避免其未被渲染的模板被用户看到。

AngularJS 将在表达式书写的位置"输出"数据。

AngularJS 表达式 很像 JavaScript 表达式：它们可以包含文字、运算符和变量。

实例 {{ 5 + 5 }} 或 {{ firstName + " " + lastName }}

    <div ng-app="">
      <p>我的第一个表达式： {{ 5 + 5 }}</p>
    </div>

**AngularJS 过滤器**

过滤器可以使用一个管道字符" | "添加到表达式和指令中。

AngularJS 过滤器可用于转换数据：

| 过滤器 | 描述 |

| -- | -- |

| currency | 格式化数字为货币格式。 |

| filter | 从数组项中选择一个子集。 |

| lowercase | 格式化字符串为小写。 |

| uppercase | 格式化字符串为大写。 |

| orderBy | 根据某个表达式排列数组。 |


####在表达式中添加过滤器

过滤器可以通过一个管道字符（|）和一个过滤器添加到表达式中：{{ lastName | uppercase }}

####向指令添加过滤器

    <ul>
      <li ng-repeat="x in names | orderBy:'country'">
        {{ x.name + ', ' + x.country }}
      </li>
    </ul>

有多种用法，要学习，可以省力

    <div ng-init="friends = [
       {name:'tom', age:16},
       {name:'jerry', age:20}, 
       {name:'garfield', age:22}]">
     
        输入过滤:<input type="text" ng-model="name" >
        <ul style="list-style-type:none">
            <li>   姓名，年龄</li>
            <li  ng-repeat="x in friends | filter:name">   
                {{ x.name + ' , ' + x.age }}
            </li>
        </ul>    
    </div>
   
---

##AngularJS 应用

AngularJS 模块（Module） 定义了 AngularJS 应用。

AngularJS 控制器（Controller） 用于控制 AngularJS 应用。

AngularJS 应用组成如下：

* View(视图), 即 HTML。
* Model(模型), 当前视图中可用的数据。即scope
* Controller(控制器), 即 JavaScript 函数，可以添加或修改属性。

ng-app指令定义了应用, ng-controller 定义了控制器。

    <div ng-app="myApp" ng-controller="myCtrl">
      名: <input type="text" ng-model="firstName"><br>
      姓: <input type="text" ng-model="lastName"><br>
      姓名: {{firstName + " " + lastName}}
    </div>
    
    <script>
      //AngularJS 模块定义应用
      var app = angular.module('myApp', []);
      //AngularJS 控制器控制应用
      app.controller('myCtrl', function($scope) {
        $scope.firstName= "John";
        $scope.lastName= "Doe";
      });
    </script> 

AngularJS 应用程序由 ng-app 定义。应用程序在 `<div>` 内运行。

ng-controller="myCtrl" 属性是一个 AngularJS 指令。用于定义一个控制器。

myCtrl 函数是一个 JavaScript 函数。

AngularJS 使用$scope 对象来调用控制器。

在 AngularJS 中， $scope 是一个应用象(属于应用变量和函数)。

控制器的 $scope （相当于作用域、控制范围）用来保存AngularJS Model(模型)的对象。

控制器在作用域中创建了两个属性 (firstName 和 lastName)。

##AngularJS Scope（很重要，有待深入研究）
Scope(作用域) 是应用在 HTML (视图) 和 JavaScript (控制器)之间的纽带。

scope是html和单个controller之间的桥梁，数据绑定就靠他了。rootscope是各个controller中scope的桥梁。

scope 是模型。拥有了$scope，我们就可以操作作用域内任何我们想要获取的对象数据。

scope 是一个 JavaScript 对象，带有属性和方法，这些属性和方法可以在视图和控制器中使用。

所有的应用都有一个 $rootScope，它可以作用在 ng-app 指令包含的所有 HTML 元素中。

$rootScope 可作用于整个应用中。是各个 controller 中 scope 的桥梁。用 rootscope 定义的值，可以在各个 controller 中使用。

如果用户未指定自己的控制器，变量就是直接挂在$rootScope这个层级上的。

**controller可以嵌套，也就是作用域可以嵌套，外层可以覆盖内层，内层如果没有值，则向外层获取。适用于方法和变量**

##AngularJS 控制器

 AngularJS 控制器 控制 AngularJS 应用程序的数据。

 ng-controller 指令定义了应用程序控制器。

 AngularJS 控制器是常规的 JavaScript 对象。

可以看AngularJS 应用那一块，控制器也可以有方法：

    <div ng-app="myApp" ng-controller="personCtrl">
      名: <input type="text" ng-model="firstName"><br>
      姓: <input type="text" ng-model="lastName"><br>
      姓名: {{fullName()}}
    </div>
    
    <script>
      var app = angular.module('myApp', []);
      app.controller('personCtrl', function($scope) {
      $scope.firstName = "John";
      $scope.lastName = "Doe";
      $scope.fullName = function() {
        return $scope.firstName + " " + $scope.lastName;
      }
    });
    </script> 

##AngularJS 服务(Service) 
***什么是服务？***

在 AngularJS 中，服务是一个函数或对象，可在你的 AngularJS 应用中使用。AngularJS 内建了30 多个服务。

 **$location服务** ，它可以返回当前页面的 URL 地址。

    <div ng-app="myApp" ng-controller="myCtrl">
      <p> 当前页面的url:</p>
      <h3>{{myUrl}}</h3>
    </div>
    
    <script>
      var app = angular.module('myApp', []);
      app.controller('myCtrl', function($scope, $location) {
      $scope.myUrl = $location.absUrl();
      });
    </script>
    
**$http服务** 

$http 是 AngularJS 应用中最常用的服务。 服务向服务器发送请求，应用响应服务器传送过来的数据。

      var app = angular.module('myApp', []);
      app.controller('myCtrl', function($scope, $http) {
        $http.get("welcome.htm").then(function (response) {
          $scope.myWelcome = response.data;
        });
      });

    
**$timeout 服务**

两秒后显示信息:

	var app = angular.module('myApp', []);
	app.controller('myCtrl', function($scope, $timeout) {
    	$scope.myHeader = "Hello World!";
    	$timeout(function () {
       		$scope.myHeader = "How are you today?";
    	}, 2000);
	});

**$interval 服务**

每两秒显示信息:

	var app = angular.module('myApp', []);
	app.controller('myCtrl', function($scope, $interval) {
    	$scope.theTime = new Date().toLocaleTimeString();
    	$interval(function () {
       		$scope.theTime = new Date().toLocaleTimeString();
    	}, 1000);
	});


**自定义服务**

hexafy就像是个类，myFunc是该类中的一个方法，也可以定义多个方法

    <script>
      var app = angular.module('myApp', []);
      app.service('hexafy', function() {
        this.myFunc = function (x) {
          return x.toString(16);
        }
      });
      app.controller('myCtrl', function($scope, hexafy) {
        $scope.hex = hexafy.myFunc(255);
      });
    </script>
    
**在过滤器中使用服务**

    <div ng-app="myApp">
      在过滤器中使用服务:
      <h1>{{255 | myFormat}}</h1>
    </div>
    <script>
      var app = angular.module('myApp', []);
      app.service('hexafy', function() {
        this.myFunc = function (x) {
          return x.toString(16);
        }
      });
      app.filter('myFormat',['hexafy', function(hexafy) {
        return function(x) {
          return hexafy.myFunc(x);
        };
      }]);
    </script>

---
##AngularJS 模块
模块定义了一个应用程序。

模块是应用程序中不同部分的容器。

模块是应用控制器的容器。

控制器通常属于一个模块。

**创建模块**

你可以通过 AngularJS 的 angular.module 函数来创建模块：

    <div ng-app="myApp">...</div>
    <script>
      var app = angular.module("myApp", []);
    </script>

"myApp" 参数对应执行应用的 HTML 元素。

现在你可以在 AngularJS 应用中添加控制器，指令，过滤器等。

**添加控制器**

你可以使用 ng-controller 指令来添加应用的控制器:

    <div ng-app="myApp" ng-controller="myCtrl">
      {{ firstName + " " + lastName }}
    </div>

    <script>
      var app = angular.module("myApp", []);
      app.controller("myCtrl", function($scope) {
        $scope.firstName = "John";
        $scope.lastName = "Doe";
      });
    </script>
    
**添加指令**

AngularJS 提供了很多内置的指令，你可以使用它们来为你的应用添加功能。

此外，你可以使用模块来为你应用添加自己的指令：

    <div ng-app="myApp" runoob-directive></div>
    <script>
      var app = angular.module("myApp", []);
      app.directive("runoobDirective", function() {
        return {
          template : "我在指令构造器中创建!"
      };
    });
    </script>
    

##总结 - 它是如何工作的呢？

ng-app 指令位于应用的根元素下。

对于单页Web应用（single page web application，SPA），应用的根通常为 <html> 元素。

一个或多个 ng-controller 指令定义了应用的控制器。每个控制器有他自己的作用域：: 定义的 HTML 元素。

AngularJS 在 HTML DOMContentLoaded 事件中自动开始。如果找到 ng-app 指令 ， AngularJS 载入指令中的模块，并将 ng-app 作为应用的根进行编译。

应用的根可以是整个页面，或者页面的一小部分，如果是一小部分会更快编译和执行。

##疑问？
1.ng-app=""和ng-app="myApp"这样有什么区别呢，运行起来是不一样的？

2.初始化顺序是怎样的，变量要不要先赋值？

html按照顺序进行加载，使用AngularJs之后，也将表达式和指令等按纯文本顺序加载，此时ng-init不发挥作用，所以在js中取不到，在加载完成定义了AngularJs应用和控制器之后，ng-init之类的才有用