package com.nivelle.base.jdk.io;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * IO
 *
 * @author nivelle
 * @date 2020/04/02
 */
public class JavaIO {


    public static void main(String[] args) throws IOException {
        File file = new File("/Users/nivellefu/IdeaProjects/programdayandnight/java-base/src/main/resources/text.txt");
        FileInputStream fileInputStream = new FileInputStream(file);

        byte[] data = new byte[1024];
        fileInputStream.read(data);

        String string = new String(data, Charset.forName("utf-8"));
        System.out.println(string);


    }
}
