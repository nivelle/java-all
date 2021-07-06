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