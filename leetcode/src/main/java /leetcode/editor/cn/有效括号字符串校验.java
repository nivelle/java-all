//给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串 s ，判断字符串是否有效。 
//
// 有效字符串需满足： 
//
// 
// 左括号必须用相同类型的右括号闭合。 
// 左括号必须以正确的顺序闭合。 
// 
//
// 
//
// 示例 1： 
//
// 
//输入：s = "()"
//输出：true
// 
//
// 示例 2： 
//
// 
//输入：s = "()[]{}"
//输出：true
// 
//
// 示例 3： 
//
// 
//输入：s = "(]"
//输出：false
// 
//
// 示例 4： 
//
// 
//输入：s = "([)]"
//输出：false
// 
//
// 示例 5： 
//
// 
//输入：s = "{[]}"
//输出：true 
//
// 
//
// 提示： 
//
// 
// 1 <= s.length <= 104 
// s 仅由括号 '()[]{}' 组成 
// 
// Related Topics 栈 字符串 
// 👍 2440 👎 0


import java.util.Stack;

//leetcode submit region begin(Prohibit modification and deletion)
class 有效括号字符串校验 {
    public boolean isValid(String s) {
        if (s.length() == 0 || s == null) return true;
        Stack<Character> stack = new Stack<>();
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '(') {
                stack.push(')');
            } else if (charArray[i] == '[') {
                stack.push(']');
            } else if (charArray[i] == '{') {
                stack.push('}');
            } else {
                //没有左半边的符号
                if (stack.isEmpty()) {
                    return false;
                }
                //弹出
                if (stack.pop() != charArray[i]) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }
}
//leetcode submit region end(Prohibit modification and deletion)
