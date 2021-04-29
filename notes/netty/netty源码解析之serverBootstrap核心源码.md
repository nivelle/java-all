### channel

- 是socket的抽象，为用户提供了关于socket状态（是连接还是断开）以及对socket的读写操作。

- 每当netty建立一个连接、都创建一个与其对应的channel 实例

### ServerBootstrap

````
 public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
        super.group(parentGroup);
        ObjectUtil.checkNotNull(childGroup, "childGroup");
        if (this.childGroup != null) {
            throw new IllegalStateException("childGroup set already");
        }
        this.childGroup = childGroup;
        return this;
    }

````