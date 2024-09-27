package com.hmdp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InvokeTest {
    public static void main(String[] args) {
        try {
            //通过全类名获取类的Class对象
            Class helloKitty = Class.forName("com.hmdp.HelloKitty");
            //将Class对象进行实例化
            HelloKitty helloKitty1 = (HelloKitty) helloKitty.newInstance();
            //获取类中所有的方法
            System.out.println("-----------------类中所有的方法名-------------------");
            Method[] methods = helloKitty.getDeclaredMethods();
            for (Method method : methods) {
                System.out.println(method.getName());
            }
            System.out.println("-----------------操纵单个方法-------------------");
            Method addNumber = helloKitty.getDeclaredMethod("addNumber", int.class, int.class);
            addNumber.invoke(helloKitty1,10,20);
            System.out.println("-----------------操纵私有方法-------------------");
            Method printStace=helloKitty.getDeclaredMethod("printStace", String.class);
            printStace.setAccessible(true);
            printStace.invoke(helloKitty1,"天蚕土豆");
            System.out.println("-----------------操纵类中的属性-------------------");
            Field field=helloKitty.getField("value");
            field.setAccessible(true);
            field.set(helloKitty1,"张其猛");
            System.out.println(helloKitty1.value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
