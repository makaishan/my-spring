package com.mks.test;

import com.mks.framework.annotation.MyAutowired;
import com.mks.framework.annotation.MyComponent;

@MyComponent
public class B {
    public void say() {
        System.out.println("B says hello.");
    }
}
