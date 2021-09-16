package com.nivelle.core.javacore.java8;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * flatMap 拍平
 *
 * @author fuxinzhong
 * @date 2021/07/02
 */
public class StreamFlatMapTest {
    /**
     * 映射，可以将一个流的元素按照一定的映射规则映射到另一个流中。分为map和flatMap：
     * <p>
     * map：接收一个函数作为参数，该函数会被应用到每个元素上，并将其映射成一个新的元素。
     * flatMap：接收一个函数作为参数，将流中的每个值都换成另一个流，然后把所有流连接成一个流。
     */

    public static void main(String[] args) {
        List<String> list = Arrays.asList("m,k,l,a", "1,3,5,7");
        List<String> listNew = list.stream().flatMap(s -> {
            String[] split = s.split(",");
            // 将每个元素转换成一个stream
            Stream<String> s2 = Arrays.stream(split);
            return s2;
        }).collect(Collectors.toList()); //合并Stream
        System.out.println("处理前的集合：" + list);
        System.out.println("处理后的集合：" + listNew);

    }
}
