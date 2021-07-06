#### git add -u //如果创建了新文件，可以执行git add -i 命令

````
-n, --dry-run         dry run
    -v, --verbose         be verbose
    - u  
    -i, --interactive     interactive picking
    -p, --patch           select hunks interactively
    -e, --edit            edit current diff and apply
    -f, --force           allow adding otherwise ignored files
    -u, --update          update tracked files
    -N, --intent-to-add   record only the fact that the path will be added later
    -A, --all             add changes from all tracked and untracked files
    --ignore-removal      ignore paths removed in the working tree (same as --no-all)
    --refresh             don't add, only refresh the index
    --ignore-errors       just skip files which cannot be added because of errors
    --ignore-missing      check if - even missing - files are ignored in dry run
    --chmod <(+/-)x>      override the executable bit of the listed files

````

#### git rm 和 git rm --cached 的区别

- 删除本地及仓库中的文件
````
git rm file
git commit -m "xxx"
git push origin master

````
- 删除仓库中的文件，保留本地的文件

如果使用 git rm --cached 删除了仓库中的文件，而且后续不想跟踪此文件，只需将此文件加入 .gitignore 中即可。


````
git rm --cached file
git commit -m "xxx"
git push origin master
````


#### 当我们想要对上一次的提交进行修改时，我们可以使用git commit –amend命令。git commit –amend既可以对上次提交的内容进行修改，也可以修改提交说明。

### git diff

- 修改后的文件在执行git diff 命令时会看到修改造成的差异
  
- 修改后的文件通过 git add 命令提交到暂存区后，再执行git diff命令将看不到该文件的修改
  
- git diff --cached;//可以看到 添加到暂存区中的文件做出的修改

- git diff --staged //可以看到提交暂存去和版本库中文件的差异

### git cherry 查看领先提交


### git stash //暂存工作区，不暂存checkout 到别的分支会丢失未提交

### git stash pop //复原工作区

### git branch -d branchUser/branchName //删除分支

-----
### git reset --soft HEAD^^ //回到最近两次提交之前

### 原理

原理： git reset的作用是修改HEAD的位置，即将HEAD指向的位置改变为之前存在的某个版本，如下图所示，假设我们要回退到版本一：

![reset操作指令.png](https://i.loli.net/2021/07/08/4mP3D8vHFiG9dLE.png)

- reset 之后，目标版本之后的版本就不见了

- 适用场景： 如果想恢复到之前某个提交的版本，且那个版本之后提交的版本我们都不要了，就可以用这种方法。

#### 实现方法

- git log 查看版本号

- 使用“git reset --hard 目标版本号”命令将版本回退

- 使用“git push -f”提交更改（此时如果用“git push”会报错，因为我们本地库HEAD指向的版本比远程库的要旧）

------------

### git revert 

原理： git revert是用于“反做”某一个版本，以达到撤销该版本的修改的目的。比如，我们commit了三个版本（版本一、版本二、 版本三），突然发现版本二不行（如：有bug），想要撤销版本二，但又不想影响撤销版本三的提交，就可以用 git revert 命令来反做版本二，生成新的版本四，这个版本四里会保留版本三的东西，但撤销了版本二的东西。如下图所示：

#### 实现方法

- 查看版本号

- 使用“git revert -n 版本号”反做，并使用“git commit -m 版本名”提交：

1. 反做，使用“git revert -n 版本号”命令(注意： 这里可能会出现冲突，那么需要手动修改冲突的文件。而且要git add 文件名。)

2. 提交，使用“git commit -m 版本名”

3. 使用“git push”推上远程库：

最重要的一点：revert 是回滚某个 commit ，不是回滚“到”某个


### git cherry-pick commitId //从众多提交中选出一个提交应用在当前工作分支中。