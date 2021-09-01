//给你一个整数 x ，如果 x 是一个回文整数，返回 true ；否则，返回 false 。 
//
// 回文数是指正序（从左向右）和倒序（从右向左）读都是一样的整数。例如，121 是回文，而 123 不是。 
//
// 
//
// 示例 1： 
//
// 
//输入：x = 121
//输出：true
// 
//
// 示例 2： 
//
// 
//输入：x = -121
//输出：false
//解释：从左向右读, 为 -121 。 从右向左读, 为 121- 。因此它不是一个回文数。
// 
//
// 示例 3： 
//
// 
//输入：x = 10
//输出：false
//解释：从右向左读, 为 01 。因此它不是一个回文数。
// 
//
// 示例 4： 
//
// 
//输入：x = -101
//输出：false
// 
//
// 
//
// 提示： 
//
// 
// -231 <= x <= 231 - 1 
// 
//
// 
//
// 进阶：你能不将整数转为字符串来解决这个问题吗？ 
// Related Topics 数学 
// 👍 1539 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class 判断整数是否为回文整数 {
    public boolean isPalindrome(int x) {
        //负数和10、100、1000 这样的数一定不是回文数，但是0是回文数
        if (x < 0 || x % 10 == 0 && x != 0) {
            return false;
        }
        //如果是回文数,将这个数反转后一定和原来的数相同
        int tmp = x;
        int reverse = 0;
        while (x > 0) {
            reverse = reverse * 10 + (x % 10);
            //下一个整数位数
            x = x / 10;
        }
        return reverse == tmp;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
