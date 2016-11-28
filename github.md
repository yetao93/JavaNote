# GitHub
###GitHub使用技巧

1. 默认搜索是从master分支搜索代码
2. 只有小于384k的代码才是可以搜索到的
3. 搜索的时候必须包含至少一个搜索关键词 如amazing language:go
4. 搜索语句不能有特殊字符如. , : ; / \ ` ' " = * ! ? # $ & + ^ | ~ < > ( ) { } [ ].

###指定搜索方式:

| 命令  | 说明 |
| -- | -- |
| octocat in:path | 搜索路径中有octocat的代码 |
| octocat in:file | 搜索文件中有octocat的代码 |
| octocat in:file,path | 搜索路径或文件中有octocat的代码 |
| display language:java | 搜索用java语言写的包含display的代码 |
| Integer | 搜索包含Integer的字段 |
| element language:xml size:100 |搜索大小为100字节的xml代码 |
| user:mozilla language:markdown | 搜索mozilla用户下用markdown写的代码 |
| android language:java fork:true | 搜索用java写的 android相关的代码并且被fork过 |
| function size:>10000 language:python | 搜索与function相关的python代码，文件大小超过10kb |
|console path:app/public language:javascript|在app/public directory目录下搜索console关键字的JS代码|
|form path:cgi-bin language:perl|搜索cgi-bin目录下包含form的perl代码|
|filename:.vimrc commands|搜索 文件名匹配\*.vimrc\* 并且包含commands的代码|
|minitest filename:test_helper path:test language:ruby|在test目录中搜索包含minitest且文件名匹配"*test_helper*"的代码|
|form path:cgi-bin extension:pm|搜索cgi-bin目录下以pm为扩展名的代码|
|icon size:>200000 extension:css|搜索超过200kb包含icon的css代码|
