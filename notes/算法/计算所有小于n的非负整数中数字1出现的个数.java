//给定一个整数 n，计算所有小于等于 n 的非负整数中数字 1 出现的个数。 
//
// 
//
// 示例 1： 
//
// 
//输入：n = 13
//输出：6
// 
//
// 示例 2： 
//
// 
//输入：n = 0
//输出：0
// 
//
// 
//
// 提示： 
//
// 
// 0 <= n <= 2 * 109 
// 
// Related Topics 数学 
// 👍 226 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class 计算所有小于n的非负整数中数字1出现的个数 {
    public int countDigitOne(int n) {
        return f(n);
    }

    private int f(int n) {
        if (n <= 0) return 0;

        String s = String.valueOf(n);
        //n的最高位
        int high = s.charAt(0) - '0';
        int pow = (int) Math.pow(10, s.length() - 1);

        int last = n - high * pow;

        if (high == 1) {
            return f(pow - 1) + last + 1 + f(last);
        } else {
            return f(pow - 1) * high + pow + f(last);
        }
    }
}
//leetcode submit region end(Prohibit modification and deletion)
