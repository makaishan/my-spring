package com.mks.framework;

import com.mks.framework.annotation.*;
import com.mks.framework.aware.MyBeanNameAware;
import com.mks.framework.init.InitializingBean;
import com.mks.framework.processor.MyBeanPostProcessor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

public class MyApplicationContext {

    private final Map<String, MyBeanDefinition> beanDefinitionMap = new HashMap<>();

    private final Map<String, Object> singletonObjects = new HashMap<>(); // 单例池

    List<MyBeanPostProcessor> beanPostProcessors = new ArrayList<>(); //后置处理器

    /**
     * 根据配置类启动spring容器
     *
     * @param configClass Spring配置类
     */
    public MyApplicationContext(Class<?> configClass) {
        // 1.扫描bean
        scan(configClass);
        // 2.实例化非懒加载的单例bean
        preInstantiateSingletons();

    }

    /**
     * 根据spring配置类扫描bean
     *
     * @param configClass Spring配置类
     */
    public void scan(Class<?> configClass) {
        // 1.获取扫描包路径
        if (!configClass.isAnnotationPresent(MyComponentScan.class)) {
            return;
        }
        MyComponentScan myComponentScan = configClass.getAnnotation(MyComponentScan.class);
        String path = myComponentScan.value();
        if (path.equals("")) {
            return;
        }
        // 2.将包名转换为文件夹路径名
        path = path.replace(".", "/");
        ClassLoader classLoader = MyApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            return;
        }
        File file = new File(resource.getFile());
        // 2.扫描该文件路径下的类，将类上面标注了@MyComponent的类加载为spring容器的一个bean
        for (File f : Objects.requireNonNull(file.listFiles())) {
            String fileName = f.getAbsolutePath();
            if (!fileName.endsWith(".class")) {
                continue;
            }
            String className = fileName.substring(fileName.indexOf(path.split("/")[0]), fileName.indexOf(".class"));
            className = className.replace("\\", ".");
            try {
                // 2.1 加载类
                Class<?> clazz = classLoader.loadClass(className);
                // 2.2 是否标注了@MyComponent
                if (!clazz.isAnnotationPresent(MyComponent.class)) {
                    continue;
                }
                // 2.3 将BeanPostProcessor加入到列表中
                if(MyBeanPostProcessor.class.isAssignableFrom(clazz)) {
                    MyBeanPostProcessor myBeanPostProcessor = (MyBeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                    beanPostProcessors.add(myBeanPostProcessor);
                }
                // 2.4 根据类的注解信息生成类的beanDefinition
                MyComponent myComponent = clazz.getAnnotation(MyComponent.class);
                String beanName = myComponent.value();
                if (beanName.equals("")) {
                    beanName = clazz.getSimpleName();
                }
                MyBeanDefinition myBeanDefinition = new MyBeanDefinition();
                myBeanDefinition.setBeanClass(clazz);
                if (clazz.isAnnotationPresent(MyLazy.class)) {
                    myBeanDefinition.setLazy(true);
                }
                if (clazz.isAnnotationPresent(MyScope.class)) {
                    MyScope myScope = clazz.getAnnotation(MyScope.class);
                    String value = myScope.value();
                    myBeanDefinition.setScope(value);
                } else {
                    myBeanDefinition.setScope("singleton");
                }
                // 2.5 将键值对(beanName, myBeanDefinition)存入map中
                beanDefinitionMap.put(beanName, myBeanDefinition);
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 实例化非懒加载的单例bean
     */
    public void preInstantiateSingletons() {
        for (String beanName : beanDefinitionMap.keySet()) {
            MyBeanDefinition myBeanDefinition = beanDefinitionMap.get(beanName);
            if (!myBeanDefinition.isLazy() && myBeanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, myBeanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }


    /**
     * 创建bean对象
     *
     * @param myBeanDefinition bean定义
     * @return Spring bean对象
     */
    public Object createBean(String beanName, MyBeanDefinition myBeanDefinition) {
        // 1.根据beanDefinition获取bean类型
        Class<?> beanClass = myBeanDefinition.getBeanClass();
        try {
            // 2.反射实例化bean
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            // 3.属性赋值(beanPostProcessor)
            for (MyBeanPostProcessor beanPostProcessor : beanPostProcessors) {
                beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
            }
            // 4.Aware回调
            if(bean instanceof MyBeanNameAware) {
                ((MyBeanNameAware) bean).setBeanName(beanName);
            }
            // 5.初始化钩子方法
            if(bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }

            return bean;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据名称获取bean
     *
     * @param beanName bean名称
     * @return Spring bean对象
     */
    public Object getBean(String beanName) {
        MyBeanDefinition myBeanDefinition = beanDefinitionMap.get(beanName);
        if (myBeanDefinition == null) {
            return null;
        }
        if (myBeanDefinition.getScope().equals("singleton")) {
            return singletonObjects.get(beanName);
        }
        if (myBeanDefinition.getScope().equals("prototype")) {
            return createBean(beanName, myBeanDefinition);
        }
        return null;
    }
}
