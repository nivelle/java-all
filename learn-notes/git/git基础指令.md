
[阮一峰的网络日志 ](http://www.ruanyifeng.com/blog/2015/12/git-cheat-sheet.html)

### 状态查询

- git status //git status 命令用于查看项目的当前状态。
              

```
git status


新增文件，未做任何操作时 执行 git staus:

位于分支 master
您的分支与上游分支 'master/master' 一致。

尚未暂存以备提交的变更：
  （使用 "git add <文件>..." 更新要提交的内容）
  （使用 "git checkout -- <文件>..." 丢弃工作区的改动）
  （提交或丢弃子模组中未跟踪或修改的内容）

        修改：     parent (修改的内容)

未跟踪的文件:
  （使用 "git add <文件>..." 以包含要提交的内容）

        learn-notes/git/

修改尚未加入提交（使用 "git add" 和/或 "git commit -a"）


```

### 撤销

- git checkout [file];//恢复暂存区的指定文件到工作区 撤销了 add 操作

- git reset [commit] //重置当前分支的指针为指定commit，同时重置暂存区，但工作区不变

- git stash ;//暂时将未提交的变化移除，稍后再移入

```
1. git stash;
保存工作目录和索引状态 WIP on gitlearn: 9fc1c68 git

2. git status
位于分支 gitlearn
您的分支与上游分支 'origin/gitlearn' 一致。

3. git stash pop
位于分支 gitlearn
您的分支与上游分支 'origin/gitlearn' 一致。

尚未暂存以备提交的变更：
  （使用 "git add <文件>..." 更新要提交的内容）
  （使用 "git checkout -- <文件>..." 丢弃工作区的改动）

        修改：     "learn-notes/git/git\345\237\272\347\241\200\346\214\207\344\273\244.md"

4. git status;
位于分支 gitlearn
您的分支与上游分支 'origin/gitlearn' 一致。

尚未暂存以备提交的变更：
  （使用 "git add <文件>..." 更新要提交的内容）
  （使用 "git checkout -- <文件>..." 丢弃工作区的改动）

        修改：     "learn-notes/git/git\345\237\272\347\241\200\346\214\207\344\273\244.md"


```

- git revert [commit] ;//新建一个commit，用来撤销指定commit;后者的所有变化都将被前者抵消，并且应用到当前分支

#### branch

```
git  chekout -b <新分支名> //新建一个分支，并切换到该分支

```

#### tag







