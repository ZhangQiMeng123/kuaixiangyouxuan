package com.hmdp.JDK动态代理;

import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.lang.reflect.Proxy;

/**
 * 代理对象工厂
 */
public class JdkProxyFactory {
    //获取代理对象
    public static Object getProxy(Object target){
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new DebugInvocationHandler(target)
        );
    }
}
