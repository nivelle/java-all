package com.nivelle.core.javabase;

import java.io.*;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/08/02
 */
public class JavaUnicodeMock {

    public static void main(String[] args) {
        writeFile1();
        readFile1();
        System.out.println("===================");
//    writeFile2();
//    readFile2();
        //JDK 7及以上才可以使用
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

}
