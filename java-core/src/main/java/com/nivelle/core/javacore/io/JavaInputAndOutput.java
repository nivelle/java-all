package com.nivelle.core.javacore.io;

import java.io.*;

/**
 * IO
 *
 * @author nivelle
 * @date 2020/04/02
 */
public class JavaInputAndOutput {


    public static void main(String[] args) throws IOException {
        File file = new File("/Users/nivellefu/IdeaProjects/programdayandnight/java-base/src/main/resources/text.txt");
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        int tempByte;
        while ((tempByte = fileInputStream.read()) != -1) {
            System.out.println(tempByte);
        }
        File file2 = new File("/Users/nivellefu/IdeaProjects/programdayandnight/java-base/src/main/resources/text.txt");
        BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new FileInputStream(file2));

        byte[] data = new byte[1024];
        while (bufferedInputStream2.read(data) != -1) {
        }
        String string = new String(data);
        System.out.println(string);


        File file3 = new File("/Users/nivellefu/IdeaProjects/programdayandnight/java-base/src/main/resources/text.txt");
        InputStreamReader InputStreamReader = new InputStreamReader(new FileInputStream(file3));
        char[] chars = new char[1024];
        while (InputStreamReader.read(chars) != -1) {

        }
        System.out.println(new String(chars));

    }
}
