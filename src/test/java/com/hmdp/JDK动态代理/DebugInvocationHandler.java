package com.hmdp.JDK动态代理;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DebugInvocationHandler implements InvocationHandler {
    private final Object target;

    public DebugInvocationHandler(Object target) {
        this.target = target;
    }


    @Override
    public Object invoke(Object proxy_, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        //被代理的方法执行前加入自己的逻辑
        System.out.println("被代理方法执行前.....");
        Object result = method.invoke(target, args);
        //被代理的方法执行后加入自己的逻辑
        System.out.println("被代理方法执行后.....");
        return result;
    }
}
