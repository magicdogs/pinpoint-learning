package com.magic.example;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by magicdog on 2017/4/12.
 */
public class Example1 {

    private String name;
    private int num;
    private String sx = "";

    public static long timer;

    public Example1(String name, int num) {
        this.name = name;
        this.num = num;
    }

    public String test(String name,String m,int x){
         System.out.println("hi " + name);
         System.out.println(m + x);
         return m + x;
    }

    public static void main(String[] args) throws Exception{
        //-javaagent:agentbootstrap-1.0-SNAPSHOT.jar
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("com/magic/example/Example1.class");
        OutputStream out = new FileOutputStream("d:/tmp/template.class");
        int len = 0;
        byte[] bt = new byte[512];
        while( (len = inputStream.read(bt) ) != -1){
            out.write(bt,0,len);
        }
        out.flush();
        out.close();
        Example1 example1 = new Example1("name",123);
        System.out.println(example1.test("123","456 ",16));
    }
}
