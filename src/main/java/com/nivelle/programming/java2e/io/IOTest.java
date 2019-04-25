package com.nivelle.programming.java2e.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOTest {


    public static void main(String args[]) throws Exception{
        //InputStream inputStream = new InputStream("nivelle /n  test /n".getBytes());

        InputStream inputStream = new ByteArrayInputStream(("Name: Anna \n" +
                "Age: 25\n" +
                "Email: anna@mailserver.com \n" +
                "Phone: 1234567890 ").getBytes());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String name1= bufferedReader.readLine();

        String name2 = bufferedReader.readLine();

        String name3 = bufferedReader.readLine();


        String name4 = bufferedReader.readLine();

        System.out.println(name1);
        System.out.println(name2);
        System.out.println(name3);
        System.out.println(name4);
    }
}
