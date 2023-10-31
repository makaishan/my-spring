package com.mks.framework.processor;

import com.mks.framework.annotation.MyComponent;
import com.mks.framework.processor.MyBeanPostProcessor;

@MyComponent
public class MyCommonAnnotationPostProcessor implements MyBeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("Resource属性赋值before " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("Resource属性赋值after " + beanName);
        return bean;
    }
}
