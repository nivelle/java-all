package com.nivelle.base.algorithms.common;

/**
 * 替换空格
 */
public class StringReplaceBlank {

    /**
     * 请实现一个函数，将一个字符串中的每个空格替换成“%20”。例如，当字符串为We Are Happy.则经过替换之后的字符串为We%20Are%20Happy。
     *
     * @param target
     * @return
     */
    public static String replaceString(String target) {
        StringBuffer res = new StringBuffer();
        int len = target.length() - 1;
        for (int i = len; i >= 0; i--) {
            if (target.charAt(i) == ' ')
                res.append("02%");
            else
                res.append(target.charAt(i));
        }
        return res.reverse().toString();

    }

    /**
     * 从后往前，每个空格后面的字符只需要移动一次。从前往后，当遇到第一个空格时，要移动第一个空格后所有的字符一次；
     * 当遇到第二个空格时，要移动第二个空格后所有的字符一次；以此类推。所以总的移动次数会更多。
     *
     * @param args
     */

    public static void main(String[] args) {
        String target = "We Are  Happy";
        String result = replaceString(target);
        System.out.println(result);
        //We%20Are%20Happy
        //We%20Are%20Happy

    }
}
