
[阮一峰的网络日志 ](http://www.ruanyifeng.com/blog/2015/12/git-cheat-sheet.html)

### 状态查询

- git status //git status 显示有变更的文件。
              
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

- git log;//显示当前分支的版本历史

- git log --stat;//显示commit历史，以及每次commit发生变更的文件

```
commit d108fe6ebdc55f43ef8aceba62cba5ef7d386614 (HEAD -> gitlearn)
commit d108fe6ebdc55f43ef8aceba62cba5ef7d386614 (HEAD -> gitlearn)
Author: fuxinzhong <fuxinzhong@zhangyue.com>
commit d108fe6ebdc55f43ef8aceba62cba5ef7d386614 (HEAD -> gitlearn)
commit d108fe6ebdc55f43ef8aceba62cba5ef7d386614 (HEAD -> gitlearn)
Author: fuxinzhong <fuxinzhong@zhangyue.com>
Date:   Mon Jun 15 18:41:15 2020 +0800

    Revert "git revert测试"
    
    This reverts commit cf2a85d7d1e93fcc3fb1060a9e44dc4555795676.

 "learn-notes/git/git\345\237\272\347\241\200\346\214\207\344\273\244.md" | 2 --
 1 file changed, 2 deletions(-)

```

- git log --follow [文件路径] //显示某个文件的版本历史，包括文件改名

- git log [tag] HEAD --pretty=format:%s //显示某个commit之后的所有变动，每个commit占据一行

- git log -5 --pretty --oneline //显示过去5次提交

- git diff :  比较工作区和暂存区之间的差异（git add ./ 之后无差别）

- git diff HEAD [ <path> … ]:比较工作区与最新本地仓库之间的差异
  
- git diff --cached [ <path>… ]:比较暂存区与最新本地仓库（本地仓库最近一次commit的内容）的差异



### 撤销

- git checkout [file];//恢复暂存区的指定文件到工作区 撤销了 add 操作

- git reset [commit] //重置当前分支的指针为指定commit,同时重置暂存区,但工作区不变

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

- git revert [commitId] ;//新建一个commit，用来撤销指定commit;后者的所有变化都将被前者抵消，并且应用到当前分支

- git reset -- hard [commitId];//重置当前分支的HEAD为指定commit，同时重置暂存区和工作区，与指定commit一致

### branch

- git branch;//列出所有本地分支

- git  checkout -b <新分支名> //新建一个分支，并切换到该分支

- git branch -r //列出所有远程分支

- git pull --rebase //表示把你的本地当前分支里的每个提交(commit)取消掉，并且把它们临时 保存为补丁(patch)(这些补丁放到".git/rebase"目录中),然后把本地当前分支更新 为最新的"origin"分支，最后把保存的这些补丁应用到本地当前分支上

- git rebase 


### tag

- git tag //列出所有的tag

- git tag [tag] //在当前commit新建一个tag

- git tag [tag] [commit] //在指定commit新建一个tag 

- git tag -d [tag] //删除本地tag

- git push origin :refs/tags/[tagName] //删除远程tag

- git show [tagName] //查看一个tag的信息

- git push [remote] [tag] //提交指定tag eg: git push origin tag v1.0.0

- git checkout -b [branch] [tag];//新建分支指向某个tag也就是某个commit

### remote

- git remote -v ;//显示所有远程分支

```
gitlearn        https://github.com/nivelle/programdayandnight.git (fetch)
gitlearn        https://github.com/nivelle/programdayandnight.git (push)
master  https://github.com/nivelle/programdayandnight.git (fetch)
master  https://github.com/nivelle/programdayandnight.git (push)
origin  git@github.com:nivelle/programdayandnight.git (fetch)
origin  git@github.com:nivelle/programdayandnight.git (push)

```

- git push [remote] --force;//强行推送当前分支到远程仓库，即使有冲突

- git push [remote] --all ;//推送所有分支到远程仓库

- git blame [file] ;//显示指定文件是什么人在什么时间修改过

### 提交删除

- git add [file1] [file2] ... // 添加指定文件到暂存区

- git add [dir] // 添加指定目录到暂存区，包括子目录

- git rm [file1] [file2] ... //删除工作区文件，并且将这次删除放入暂存区

- git rm --cached [file] //停止追踪指定文件，但该文件会保留在工作区

- git mv [file-original] [file-renamed] // git mv [file-original] [file-renamed]

- git commit -a //提交工作区自上次commit之后的变化，直接到仓库区

- git commit -v //提交时显示所有diff信息

- git commit --amend -m [message] // 使用一次新的commit，替代上一次提交;如果代码没有任何新变化，则用来改写上一次commit的提交信息

### 冲突标志

```
<<<<<<< HEAD

本地代码

=======

拉下来的代码

>>>>>>>

```