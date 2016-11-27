# Git

## 安装Git

下载安装好后，打开Git Bash，进行设置

```
$ git config --global user.name "Your Name"
$ git config --global user.email "email@example.com"
```

---

## 创建删除版本库repository

首先创建一个空文件夹，使用git init命令：

	$ mkdir learngit
	$ cd learngit
	$ pwd /Users/michael/learngit
	$ git init


这时会多一个.git的文件夹

删除版本库

	$ rm -rf .git

这样就会删掉.git文件夹

---

## 把文件添加到版本库

首先这里再明确一下，所有的版本控制系统，其实只能跟踪文本文件的改动，比如TXT文件，网页，所有的程序代码等等，Git也不例外。版本控制系统可以告诉你每次的改动，比如在第5行加了一个单词“Linux”，在第8行删了一个单词“Windows”。而图片、视频这些二进制文件，虽然也能由版本控制系统管理，但没法跟踪文件的变化，只能把二进制文件每次改动串起来，也就是只知道图片从100KB改成了120KB，但到底改了啥，版本控制系统不知道，也没法知道。

不幸的是，Microsoft的Word格式是二进制格式，因此，版本控制系统是没法跟踪Word文件的改动的，前面我们举的例子只是为了演示，如果要真正使用版本控制系统，就要以纯文本方式编写文件。**千万不要使用Windows自带的记事本编辑任何文本文件。**

在learngit目录下新建一个readme.txt文件

`$ git add readme.txt`

`$ git add .`添加所有文件

`$ git commit -m "wrote a readme file"`

-m后面是提交的说明，commit一次可以提交很多文件，所以可以多次add文件，一次提交

---

## 有文件被修改之后

`$ git status`命令可以让我们时刻掌握仓库当前的状态，比如有无文件被修改

`$ git diff`命令让我们可以看到具体修改的内容，如`$ git diff readme.txt`

---

## 查看历史记录与版本回退

`$ git log`

查看提交修改的历史记录，可以加上参数`--pretty=oneline`简洁查看，前面很长的一串字母数字的前7位是commit id

在Git中，用HEAD表示当前版本，上一个版本就是HEAD^，上上一个版本就是HEAD^^，当然往上100个版本写100个^比较容易数不过来，所以写成HEAD~100，现在要回退到上一个版本：

`$ git reset --hard HEAD^`

此时就回到了上一版本，用`$ git log`查看版本记录，会发现最新版已经不见了，再要回到最新版，必须知道最新版的commit id，使用

`$ git reset --hard 12345`

指定回到该版本。

如果不知道最新版的commit id，可以用

`$ git reflog`

查看历史操作记录。

---

## 名词解释

**工作区**：就是你在电脑里能看到的目录，比如我的learngit文件夹就是一个工作区。

**版本库**：工作区有一个隐藏目录.git，这个不算工作区，而是Git的版本库。Git的版本库里存了很多东西，其中最重要的就是称为stage（或者叫index）的暂存区，还有Git为我们自动创建的第一个分支master，以及指向master的一个指针叫HEAD。

前面讲了我们把文件往Git版本库里添加的时候，是分两步执行的：

第一步是用git add把文件添加进去，实际上就是把文件修改添加到暂存区；

第二步是用git commit提交更改，实际上就是把暂存区的所有内容提交到当前分支。

如果：第一次修改 -&gt; git add -&gt; 第二次修改 -&gt; git commit，这样第二次修改不会被提交，必须要add之后才能提交。

---

## 撤销修改

**丢弃工作区的修改**

如果工作区的文件修改过了，不满意，要恢复到上一个版本的状态，即版本库或暂存区的内容。可使用

`$ git checkout -- file`

将版本库或暂存区的内容拿过来，此时暂存区不变。

_自己操作：_如果同一个文件，在版本库为A，暂存区为B，工作区为C，此时用checkout命令，会将暂存区的B替换掉工作区的C，多次使用没有效果，即不会用版本库的A来替换

**丢弃暂存区**

`$ git reset HEAD file`

可以把暂存区的修改撤销掉（unstage），重新放回工作区。当我们用HEAD时，表示最新的版本。

**小结**

场景1：当你改乱了工作区某个文件的内容，想直接丢弃工作区的修改时，用命令git checkout -- file。

场景2：当你不但改乱了工作区某个文件的内容，还添加到了暂存区时，想丢弃修改，分两步，第一步用命令git reset HEAD file，清空了暂存区，就回到了场景1，第二步按场景1操作。

场景3：已经提交了不合适的修改到版本库时，想要撤销本次提交，参考版本回退一节，不过前提是没有推送到远程库。

---

## 删除文件

先新建一个文件并添加到版本库中：

```
$ git add test.txt
$ git commit -m "add test.txt"
```

然后删除这个文件，用`$ git status`能看出发生了哪些变化，要从版本库中删除这个文件：

```
$ git rm test.txt
$ git commit -m "remove test.txt"
```

如果只删除了工作区的文件，版本库中还有，想要恢复到工作区：

`$ git checkout -- test.txt`

`git checkout`其实是用版本库里的版本替换工作区的版本，无论工作区是修改还是删除，都可以“一键还原”。

**小结**

命令git rm用于删除一个文件。如果一个文件已经被提交到版本库，那么你永远不用担心误删，但是要小心，你只能恢复文件到最新版本，你会丢失最近一次提交后你修改的内容。

---

## 将本地库添加到远程库

**第1步**：创建SSH Key。在用户主目录下，看看有没有.ssh目录，如果有，再看看这个目录下有没有id\_rsa和id\_rsa.pub这两个文件，如果已经有了，可直接跳到下一步。如果没有，打开Shell（Windows下打开Git Bash），创建SSH Key：

`$ ssh-keygen -t rsa -C "youremail@example.com"`

如果一切顺利的话，可以在用户主目录里找到.ssh目录，里面有id\_rsa和id\_rsa.pub两个文件，这两个就是SSH Key的秘钥对，id\_rsa是私钥，不能泄露出去，id\_rsa.pub是公钥，可以放心地告诉任何人。

**第2步**：登陆GitHub，打开“Account settings”，“SSH Keys”页面。然后，点“Add SSH Key”，填上任意Title，在Key文本框里粘贴id\_rsa.pub文件的内容。

**第3步**：现在的情景是，你已经在本地创建了一个Git仓库后，又想在GitHub创建一个Git仓库，并且让这两个仓库进行远程同步，这样，GitHub上的仓库既可以作为备份，又可以让其他人通过该仓库来协作，真是一举多得。

首先，登陆GitHub，然后，在右上角找到“Create a new repo”按钮，创建一个新的仓库learngit，然后将其与本地仓库关联

`$ git remote add myorigin git@github.com:yetao93/SignalBaiduMap.git`

下一步，就可以把本地库的所有内容推送到远程库上：

`git push -u myorigin master`

把本地库的内容推送到远程，用git push命令，实际上是把当前分支master推送到远程。由于远程库是空的，我们第一次推送master分支时，加上了-u参数，Git不但会把本地的master分支内容推送的远程新的master分支，还会把本地的master分支和远程的master分支关联起来，在以后的推送或者拉取时就可以简化命令。

从现在起，只要本地作了提交，就可以通过命令：

`$ git push myorigin master`

把本地master分支的最新修改推送至GitHub，现在，你就拥有了真正的分布式版本库！

有时候会报错，可能原因是github有中readme文件，本地没有，可用以下命令

`git pull --rebase myorigin master`

注意，pull=fetch+merge，进行代码合并，再提交

### 远程交互详解

![git工作流程图](C:\Laptop\info center\Import\yetao93\skye\git.jpg)

#### git clone

远程操作的第一步，通常是从远程主机克隆一个版本库，这时就要用到`$ git clone <版本库的网址>`命令。

该命令会在本地主机生成一个目录，与远程主机的版本库同名。如果要指定不同的目录名，可以将目录名作为命令的第二个参数`$ git clone <版本库的网址> <本地目录名>`

支持多种协议。

#### git remote

`$ git remote`命令列出所有远程主机。

使用-v选项，可以参看远程主机的网址。`$ git remote -v`

克隆版本库的时候，所使用的远程主机自动被Git命名为origin。如果想用其他的主机名，需要用git clone命令的-o选项指定。`$ git clone -o jQuery https://github.com/jquery/jquery.git`

`$ git remote show <主机名>`可以查看该主机的详细信息。

`$ git remote add <主机名> <网址>`命令用于添加远程主机。

`$ git remote rm <主机名>`命令用于删除远程主机。

`$ git remote rename <原主机名> <新主机名>`命令用于远程主机的改名。

#### git fetch

一旦远程主机的版本库有了更新（Git术语叫做commit），需要将这些更新取回本地，这时就要用到git fetch命令。

`$ git fetch <远程主机名>`将某个远程主机的更新，全部取回本地。

默认情况下，git fetch取回所有分支（branch）的更新。如果只想取回特定分支的更新，可以指定分支名。

`$ git fetch <远程主机名> <分支名>`

所取回的更新，在本地主机上要用"远程主机名\/分支名"的形式读取。比如origin主机的master，就要用origin\/master读取。

`$ git branch -r`命令可以用来查看远程分支

`$ git branch -a`查看所有分支。

取回远程主机的更新以后，可以在它的基础上，使用git checkout命令创建一个新的分支。

`$ git checkout -b newBrach origin/master`

上面命令表示，在origin\/master的基础上，创建一个新分支。

此外，也可以使用git merge命令或者git rebase命令，在本地分支上合并远程分支。

```
$ git merge origin/master
# 或者
$ git rebase origin/master
```

上面命令表示在当前分支上，合并origin\/master。

#### git pull

git pull命令的作用是，取回远程主机某个分支的更新，再与本地的指定分支合并。它的完整格式稍稍有点复杂。

```
$ git pull <远程主机名> <远程分支名>:<本地分支名>
```

比如，取回origin主机的next分支，与本地的master分支合并，需要写成下面这样。

```
$ git pull origin next:master
```

如果远程分支是与当前分支合并，则冒号后面的部分可以省略。

```
$ git pull origin next
```

实质上，这等同于先做git fetch，再做git merge。

在某些场合，Git会自动在本地分支与远程分支之间，建立一种追踪关系（tracking）。比如，在git clone的时候，所有本地分支默认与远程主机的同名分支，建立追踪关系，也就是说，本地的master分支自动"追踪"origin\/master分支。

Git也允许手动建立追踪关系。

```
git branch --set-upstream master origin/next
```

上面命令指定master分支追踪origin\/next分支。

如果当前分支与远程分支存在追踪关系，git pull就可以省略远程分支名。

```
$ git pull origin
```

上面命令表示，本地的当前分支自动与对应的origin主机"追踪分支"（remote-tracking branch）进行合并。

如果当前分支只有一个追踪分支，连远程主机名都可以省略。

```
$ git pull
```

上面命令表示，当前分支自动与唯一一个追踪分支进行合并。

### git push

git push命令用于将本地分支的更新，推送到远程主机。它的格式与git pull命令相仿。

```
$ git push <远程主机名> <本地分支名>:<远程分支名>
```

注意，分支推送顺序的写法是&lt;来源地&gt;:&lt;目的地&gt;，所以git pull是&lt;远程分支&gt;:&lt;本地分支&gt;，而git push是&lt;本地分支&gt;:&lt;远程分支&gt;。

如果省略远程分支名，则表示将本地分支推送与之存在"追踪关系"的远程分支（通常两者同名），如果该远程分支不存在，则会被新建。

```
$ git push origin master
```

上面命令表示，将本地的master分支推送到origin主机的master分支。如果后者不存在，则会被新建。

如果省略本地分支名，则表示删除指定的远程分支，因为这等同于推送一个空的本地分支到远程分支。

```
$ git push origin :master
# 等同于
$ git push origin --delete master
```

上面命令表示删除origin主机的master分支。

如果当前分支与远程分支之间存在追踪关系，则本地分支和远程分支都可以省略。

```
$ git push origin
```

上面命令表示，将当前分支推送到origin主机的对应分支。

如果当前分支只有一个追踪分支，那么主机名都可以省略。

```
$ git push
```

如果当前分支与多个主机存在追踪关系，则可以使用-u选项指定一个默认主机，这样后面就可以不加任何参数使用git push。

```
$ git push -u origin master
```

上面命令将本地的master分支推送到origin主机，同时指定origin为默认主机，后面就可以不加任何参数使用git push了。

不带任何参数的git push，默认只推送当前分支，这叫做simple方式。此外，还有一种matching方式，会推送所有有对应的远程分支的本地分支。Git 2.0版本之前，默认采用matching方法，现在改为默认采用simple方式。如果要修改这个设置，可以采用git config命令。

```
$ git config --global push.default matching
# 或者
$ git config --global push.default simple
```

还有一种情况，就是不管是否存在对应的远程分支，将本地的所有分支都推送到远程主机，这时需要使用--all选项。

```
$ git push --all origin
```

上面命令表示，将所有本地分支都推送到origin主机。

如果远程主机的版本比本地版本更新，推送时Git会报错，要求先在本地做git pull合并差异，然后再推送到远程主机。这时，如果你一定要推送，可以使用--force选项。

```
$ git push --force origin 
```

上面命令使用--force选项，结果导致远程主机上更新的版本被覆盖。除非你很确定要这样做，否则应该尽量避免使用--force选项。

最后，git push不会推送标签（tag），除非使用--tags选项。

```
$ git push origin --tags
```

---

## 分支管理

首先，我们创建dev分支，然后切换到dev分支：

`$ git checkout -b dev`

相当于：

```
$ git branch dev     创建分支dev
$ git checkout dev   切换到分支dev
```

可以用`$ git branch`查看当前及所有分支，此时所有操作都是在dev分支上，不影响master主分支

`$ git merge dev`命令将dev合并到master

`$ git branch -d dev`合并完成后删除dev分支

**小结**

Git鼓励大量使用分支：

查看分支：`git branch`

创建分支：`git branch <name>`

切换分支：`git checkout <name>`

创建+切换分支：`git checkout -b <name>`

合并某分支到当前分支：`git merge <name>`

删除分支：`git branch -d <name>`

**分支冲突**：有时两个分支不能快速合并，就要先解决冲突，再提交合并，用`git log --graph`查看分支合并图

**bug分支**：修复bug时，我们会通过创建新的bug分支进行修复，然后合并，最后删除；

当手头工作没有完成时，先把工作现场git stash一下，然后去修复bug，修复后，再git stash pop，回到工作现场。

---

## 多人协作（不够详细）

查看远程库信息，使用`git remote -v`；

本地新建的分支如果不推送到远程，对其他人就是不可见的；

从本地推送分支，使用`git push origin branch-name`，如果推送失败，先用`git pull`抓取远程的新提交；

在本地创建和远程分支对应的分支，使用`git checkout -b branch-name origin/branch-name`，本地和远程分支的名称最好一致；

建立本地分支和远程分支的关联，使用`git branch --set-upstream branch-name origin/branch-name`；

从远程抓取分支，使用`git pull`，如果有冲突，要先处理冲突。

---

