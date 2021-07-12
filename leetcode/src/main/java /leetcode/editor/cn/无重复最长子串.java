//给定一个字符串，请你找出其中不含有重复字符的 最长子串 的长度。 
//
// 
//
// 示例 1: 
//
// 
//输入: s = "abcabcbb"
//输出: 3

import java.util.HashMap;


//解释: 因为无重复字符的最长子串是 "abc"，所以其长度为 3。
// 
//
// 示例 2: 
//
// 
//输入: s = "bbbbb"
//输出: 1
//解释: 因为无重复字符的最长子串是 "b"，所以其长度为 1。
// 
//
// 示例 3: 
//
// 
//输入: s = "pwwkew"
//输出: 3
//解释: 因为无重复字符的最长子串是 "wke"，所以其长度为 3。
//     请注意，你的答案必须是 子串 的长度，"pwke" 是一个子序列，不是子串。
// 
//
// 示例 4: 
//
// 
//输入: s = ""
//输出: 0
// 
//
// 
//
// 提示： 
//
// 
// 0 <= s.length <= 5 * 104 
// s 由英文字母、数字、符号和空格组成 
// 
// Related Topics 哈希表 双指针 字符串 Sliding Window 
// 👍 5573 👎 0
//定义一个指针维持一个滑动窗口，start指向子串的第一个位置，end指向子串的最后一个位置
//每次通过end将窗口向右滑动，如果遇到和窗口内元素重复的元素就让start向前移动

//leetcode submit region begin(Prohibit modification and deletion)

/**
 * 无重复字符的最长字串
 */
class Solution {
    public int lengthOfLongestSubstring(String s) {
        if (s.length() == 0 || s == null) return 0;
        int res = 0;
        //子串开始
        int start = 0;
        HashMap<Character, Integer> map = new HashMap<>();
        for (int end = 0; end < s.length(); end++) {
            if (map.containsKey(s.charAt(end))) {
                //获取重复的第一个字符的位移，子串开始位置更新
                start = Math.max(start, map.get(s.charAt(end)) + 1);
            }
            //字符串和下标
            map.put(s.charAt(end),end);
            res = Math.max(res,end-start+1);
        }
        return res;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
