

###  频道的订阅与退订

当一个客户端执行SUBSCRIBE命令订阅某个某个频道或某些频道的时候,这个客户端与被订阅频道之间就建立起了一种订阅关系.

redis将所有频道的订阅关系都保存在服务器状态的pubsub_cahnnels字典里面,这个字典的建是某个被订阅的频道,而键的值则是一个链表,链表里面记录了所有订阅这个频道的客户端:

```
struct redisServer{
    //...
    
    //保存所有频道的订阅关系
    dict *pubsub_cahannels;
    //...
}

```

### redis发布订阅

redis提供了发布订阅功能,可用于消息的传输,Redis的发布订阅机制包括三个部分,发布者 ,订阅者和Channel.

![image](http://7xpuj1.com1.z0.glb.clouddn.com/%E5%8F%91%E5%B8%83%E8%AE%A2%E9%98%85.png)

发布者和订阅者都是Redsi客户端,Channel则为Redis服务器端,发布者将消息发送到某个频道,订阅了这个频道的订阅者就能接收到这条消息.Redis的这种发布订阅机制与基于主题的发布订阅类似,Channel相当于主题.

#### Redis发布订阅功能

- 发布消息

redis采用PUBLISH命令发送消息,其返回值为接收到该消息的订阅者数量

![image](http://img.blog.csdn.net/20170415154014186?/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd3EyNTI2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- 订阅某个频道

redis采用SUBSCRIBE命令订阅某个频道,其返回值包括客户端订阅的频道,目前已经订阅的频道数量,以及接收到的消息,其中subscribe表示已经成功订阅了某个频道.

![image](http://img.blog.csdn.net/20170415155055608?/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd3EyNTI2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

- 模式匹配

模式匹配功能允许客户端订阅符合模式的频道,Redsi采用PSUBSCRIBE订阅䄦 某个模式所有频道,用""表示模式,可以被任意值取代.

![image](http://img.blog.csdn.net/20170415155121296?/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd3EyNTI2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

假设客户端同时订阅了某种模式和符合该模式的某个频道，那么发送给这个频道的消息将被客户端接收到两次，只不过这两条消息的类型不同，一个是message类型，一个是pmessage类型，但其内容相同

- 取消订阅

Redis采用UNSUBSCRIBE和PUNSUBSCRIBE命令取消订阅，其返回值与订阅类似。 
由于Redis的订阅操作是阻塞式的，因此一旦客户端订阅了某个频道或模式，就将会一直处于订阅状态直到退出。在SUBSCRIBE，PSUBSCRIBE，UNSUBSCRIBE和PUNSUBSCRIBE命令中，其返回值都包含了该客户端当前订阅的频道和模式的数量，当这个数量变为0时，该客户端会自动退出订阅状态。

### Redis发布订阅实现

- SUBSCRIBE

![image](http://img.blog.csdn.net/20170415155201828?/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd3EyNTI2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

当客户端订阅某个频道时,Redis需要将该频道和该客户端绑定首先,在客户端结构体client中,有个属性为pubsub_channels,该属性表明该客户端订阅的所有频道,它是一个字典类型,,通过哈希表实现.其中每个元素都包含了一个键值对以及向下一个元素指针,每次订阅都要向其中插入一个节点,**键表示订阅的频道,值为空.**

- PSUBSCRIBE

![image](http://img.blog.csdn.net/20170415155254226?/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvd3EyNTI2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

当客户端订阅某个模式时，Redis同样需要将该模式和该客户端绑定。首先，在结构体client中，有一个属性为pubsub_patterns，该属性表示该客户端订阅的所有模式，它是一个链表类型，每个结点包括了订阅的模式和指向下一个结点的指针，每次订阅某个模式时，都要向其中插入一个结点。然后，在结构体redisServer中，有一个属性也叫pubsub_patterns，它表示了该服务器端中的所有模式和订阅了这些模式的客户端，它也是一个链表类型，插入结点时，每个结点都要包含订阅的模式，以及订阅这个模式的客户端，和指向下一个结点的指针。 

当客户端向某个频道发送消息时，Redis首先在结构体redisServer中的pubsub_channels中找出键为该频道的结点，遍历该结点的值，即遍历订阅了该频道的所有客户端，将消息发送给这些客户端。然后，遍历结构体redisServer中的pubsub_patterns，找出包含该频道的模式的结点，将消息发送给订阅了该模式的客户端。