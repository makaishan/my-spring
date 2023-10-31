package com.mks.test;

import com.mks.framework.MyApplicationContext;

public class Test {
    public static void main(String[] args) {
        MyApplicationContext myApplicationContext = new MyApplicationContext(SpringConfig.class);
        A a = (A) myApplicationContext.getBean("A");
        a.say();
    }
}
