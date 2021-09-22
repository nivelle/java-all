package com.nivelle.core.javacore.base;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.SortedMap;

/**
 * 字符码
 *
 * @author fuxinzhong
 * @date 2021/08/02
 */
public class JavaUnicodeMock {

    public static void main(String[] args) throws UnsupportedEncodingException{
        writeFile1();
        readFile1();
        System.out.println("===================");
//    writeFile2();
//    readFile2();
        //JDK 7及以上才可以使用

        String a = "我是猪";

        // 先按UTF-8获取，转化成GBK编码
        String b = new String(a.getBytes("UTF-8"), "GBK");
        System.out.println(b);

        // 按GBK获取，又转换成UTF-8的编码
        String c = new String(b.getBytes("GBK"), "UTF-8");
        System.out.println(c);

        ys();
    }

    /*
     * 写入
     */
    public static void writeFile1() {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        try {
            // 节点类
            fos = new FileOutputStream("./book.txt");
            // 转化类
            osw = new OutputStreamWriter(fos,"UTF-8");
            // 装饰类
            bw = new BufferedWriter(osw);
            bw.write("曾经梦想仗剑走天涯");
            bw.newLine();
            bw.write("看一看世界的繁华");
            bw.flush();
        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            try {
                // 关闭最后一个类。会将所有的底层流都关闭掉
                bw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void writeFile2() {
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("G:/temp1/yx.txt")))){
            bw.write("曾经的你");
            bw.flush();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    /*
     * 读取
     */
    public static void readFile1() {
        FileInputStream fis ;
        InputStreamReader isr;
        BufferedReader br = null;
        String line;
        try {
            fis = new FileInputStream("G:/temp1/yx.txt");
            isr = new InputStreamReader(fis,"UTF-8");
            br = new BufferedReader(isr);
            while((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }finally{
            try {
                // 关闭最后一个类，会将所有的底层流都关闭
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void readFile2() {
        String line;
        // try-resource 语句，自动关闭资源
        try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("G:/temp1/yx.txt")))){
            while((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void ys() {
        // 获取当前计算机编码
        Charset aa = Charset.defaultCharset();
        System.out.println(aa.name());

        // 输出所有 支持的字符集
        SortedMap<String, Charset> sm = Charset.availableCharsets();
        Set<String> keyset = sm.keySet();
        System.out.println("支持的所有字符集");
        for(String s: keyset) {
            System.out.println(s);
        }
    }

}
