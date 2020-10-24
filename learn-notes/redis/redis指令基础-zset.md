### zset

#### 1.zadd key score member [[score member] [score member] …]

````
返回值:被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
    
//将一个或多个 member 元素及其 score 值加入到有序集 key 当中。
  
// 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。
   
// score 值可以是整数值或双精度浮点数。
   
````

#### 2.zscore key member


````
返回值: member 成员的 score 值，以字符串形式表示。
     
//返回有序集 key 中，成员 member 的 score 值。
  
//如果 member 元素不是有序集 key 的成员，或 key 不存在，返回 nil 。
  
````

#### 3.zincrby key increment member

````
返回值: member 成员的新 score 值，以字符串形式表示。
     
//为有序集 key 的成员 member 的 score 值加上增量 increment 。
  
//可以通过传递一个负数值 increment ，让 score 减去相应的值，比如 ZINCRBY key -5 member ，就是让 member 的 score 值减去 5 。
  
//当 key 不存在，或 member 不是 key 的成员时， ZINCRBY key increment member 等同于 ZADD key increment member 。
  
//score 值可以是整数值或双精度浮点数。
  
````

#### 4.zcard key

````
返回值: 当 key 存在且是有序集类型时，返回有序集的基数。 当 key 不存在时，返回 0 。
     
````

#### 5. zcount key min max

````
返回值: score 值在 min 和 max 之间的成员的数量。
     
//返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。
  
````

#### 6. zrange key start[下标] stop [WITHSCORES]

````
// 返回有序集 key 中，指定区间内的成员。
   
// 其中成员的位置按 score 值递增(从小到大)来排序。
   
// 具有相同 score 值的成员按字典序(lexicographical order )来排列。
   
//下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。
  
// 可以通过使用 WITHSCORES 选项，来让成员和它的 score 值一并返回，返回列表以 value1,score1, ..., valueN,scoreN 的格式表示。
   
````

#### 7. zrevrange key start stop [WITHSCORES]

````
//返回有序集 key 中，指定区间内的成员。
  
//其中成员的位置按 score 值递减(从大到小)来排列。 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order)排列。
  
````

#### 8. zrangebyscore key min[最小分数] max[最大分数] [WITHSCORES] [LIMIT offset count]

````
返回值: 指定区间内，带有 score 值(可选)的有序集成员的列表。
     
// 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
   
// 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
   
// 可选的 LIMIT 参数指定返回结果的数量及区间, 注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集,此过程最坏复杂度为 O(N) 时间。

// min 和 max 可以是 -inf 和 +inf , 这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
   
// 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
   
````

#### 9. zrevrangebyscore key max[最大分数] min[最小分数] [WITHSCORES] [LIMIT offset count]

````
// 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
   
// 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
   
````

#### 10. zrank key member

````
返回值: 如果 member 是有序集 key 的成员，返回 member 的排名。 如果 member 不是有序集 key 的成员，返回 nil 。
     

// 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。
   
// 排名以 0 为底，也就是说， score 值最小的成员排名为 0 。
   
````

#### 11. zrevrank key member

````
返回值: 如果 member 是有序集 key 的成员，返回 member 的排名。 如果 member 不是有序集 key 的成员，返回 nil 。
     
// 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递减(从大到小)排序。
   
// 排名以 0 为底，也就是说， score 值最大的成员排名为 0 。
   

````

#### 12. zrem key member [member …]

````
返回值: 被成功移除的成员的数量，不包括被忽略的成员。
     
// 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。
   
// 当 key 存在但不是有序集类型时，返回一个错误。
  
````

#### 13. zremrangebyrank key start stop

````
// 移除有序集 key 中，指定排名(rank)区间内的所有成员。
   
// 区间分别以下标参数 start 和 stop 指出，包含 start 和 stop 在内。
   
// 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。
   
// 被移除成员的数量。
   

````
 
#### 14. zremrangebyscore key min max

````
返回值: 被移除成员的数量

//移除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
  
````

#### 15. zrangebylex  key min max [LIMIT offset count]

````
当有序集合的所有成员都具有相同的分值时， 有序集合的元素会根据成员的字典序（lexicographical ordering）来进行排序， 而这个命令则可以返回给定的有序集合键 key 中， 值介于 min 和 max 之间的成员。

````






















