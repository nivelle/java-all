### 解题思路
此处撰写解题思路
由于是逆序存储，即第一个结点就是个位，第二个是十位，第三个是百位
1.首先个位和个位相加会产生进位例如7+3=10，进位位1，0就是我们要存到新链表上的数据。
2.可以将这两个链表看成是长度相同的，进行判断，短的那一个就补零和长的那一个凑成长度相同的。
3.对于不同长度的两个链表怎样进行判断谁长谁短while(l1 != null || l2 != null)利用一个while循环，只要有一个不为空就可以往下走
4.怎样补零，对于短的链表，以为要将l1和l2的值进行相加，而且是逐位相加，sum = l1.val+l2.val+carry,carry为进位，刚开始个位相加的时候进位位0即可以初始化carry=0，例如7+3+carry=sum,即sum为10,然后会产生新的进位,利用sum/10得到新的carry,再来可以利用sum%10得到我们实际上要存到新的链表上的值;那么直接写sum = l1.val+l2.val+carry?由于7-8-9和3-2在l2为null时，l1还指向9，此时l2.val会出现空指针异常,所以我们可以利用一个条件表达式，判断l1和l2是否为空，将其值l1.val或将0赋给一个变量保存，l2同理
5.可以 ListNode actuallyValue = new ListNode(sum % 10);将新产生的值放到一个结点中
6.怎样将每次得到的值凑成一个单链表，在第一次产生结点时，可以定义一个ListNode pre和ListNode cur,pre用于保存新链表的头结点，cur用于移动,因为还会有新的结点插入
8.while循环全走完了,到最后如果进位为1，则将该1值增加入新链表.并且返回pre.next，因为刚开始pre和cur指向相同的结点,令cur.next = actuallyValue,即pre也指向了一个结点,这个结点就是新链表的头结点
### 代码
```java
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode pre = new ListNode(0);//初始化预先指针,用于新的链表
    	ListNode cur = pre;
    	 int carry = 0;
    	 
    	 //怎样解决两个链表的长度问题
    	 while(l1 != null || l2 != null) {
    		 //长度没检索完，怎样补零
//    		 if(l2 == null) {
//    			 ListNode increNodel2 = new ListNode(0);
//    		 }
//    		 if(l1 == null) {
//    			 ListNode increNodel1 = new ListNode(0);
//    		 }
    		 
    		 int x = (l1 != null)?l1.val:0;
    		 int y = (l2 != null)?l2.val:0;
    		 int sum = 0;
    		 sum = x + y + carry;//sum不能做累加需要定义成局部变量
        	 carry = sum / 10;//获取新的进位
        	 //实际上留下来的值是0,需要保存到新的链表中
        	 ListNode actuallyValue = new ListNode(sum % 10);
        	 //pre和cur
        	 cur.next = actuallyValue;//cur.next将pre和新增的结点连起来了，因为cur和pre刚开始是指向同一个结点的
        	 cur = cur.next;//cur向前移动
        	//向前走遍历
        	 
        	 if(l1 != null)
        	 l1 = l1.next;
        	 if(l2 != null)
        	 l2 = l2.next;
        	 
    	 }
    	 if(carry == 1) {
    		 ListNode increNode = new ListNode(1);
    		 cur.next = increNode;
    	 }
    	 return pre.next;
    }
}
```