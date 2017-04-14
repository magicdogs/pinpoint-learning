package com.magic.example;

/**
 * Created by magicdog on 2017/4/12.
 */
public class Example1 {

    private String name;
    private int num;

    public Example1(String name, int num) {
        this.name = name;
        this.num = num;
    }

    public String test(String name){
        System.out.println("hi " + name);
        return "hello " + name;
    }

    public static void main(String[] args) {
        //-javaagent:agentbootstrap-1.0-SNAPSHOT.jar
        Example1 example1 = new Example1("name",123);
        System.out.println(example1);
    }
}
