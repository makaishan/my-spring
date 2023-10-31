package com.mks.test;

import com.mks.framework.annotation.MyAutowired;
import com.mks.framework.annotation.MyComponent;
import com.mks.framework.aware.MyBeanNameAware;
import com.mks.framework.init.InitializingBean;

@MyComponent
public class A implements MyBeanNameAware, InitializingBean {

    @MyAutowired
    private B b;

    private String beanName;

    public void say(){
        System.out.println("bean name is " + beanName);
        System.out.println("A says hello.");
        System.out.println(b);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("属性赋值后。。。");
    }
}
