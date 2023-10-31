package com.mks.test;

import com.mks.framework.MyApplicationContext;

public class Test {
    public static void main(String[] args) {
        MyApplicationContext myApplicationContext = new MyApplicationContext(SpringConfig.class);
        B b = (B) myApplicationContext.getBean("B");
        b.say();
    }
}
