//给定一个包含大写字母和小写字母的字符串，找到通过这些字母构造成的最长的回文串。 
//
// 在构造过程中，请注意区分大小写。比如 "Aa" 不能当做一个回文字符串。 
//
// 注意: 
//假设字符串的长度不会超过 1010。 
//
// 示例 1: 
//
// 
//输入:
//"abccccdd"
//
//输出:
//7
//
//解释:
//我们可以构造的最长的回文串是"dccaccd", 它的长度是 7。
// 
// Related Topics 贪心 哈希表 字符串 
// 👍 306 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    /**
     * 时间复杂度：O(N)，其中 N 为字符串 s 的长度。我们需要遍历每个字符一次
     */
    public int longestPalindrome(String s) {
        int[] count = new int[128];
        int length = s.length();
        for (int i = 0; i < length; ++i) {
            //第 i 位的字符 c
            char c = s.charAt(i);
            //count[c] 统计字符c出现的次数
            count[c]++;
        }
        int ans = 0;
        for (int v : count) {
            //ans 字符char 可以出现的次数
            ans += v / 2 * 2;
            //字符v出现次数是奇数，而且总次数已经是偶数,则+1，可以放在中间一次
            if (v % 2 == 1 && ans % 2 == 0) {
                //ans 变为奇数后 就不再使用奇数字符了
                ans++;
            }
        }
        return ans;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
