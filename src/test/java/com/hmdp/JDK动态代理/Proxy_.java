package com.hmdp.JDK动态代理;

import com.hmdp.静态代理.SmService;

public class Proxy_ {
    public static void main(String[] args) {
        SmService smService = (SmService) JdkProxyFactory.getProxy(new SmServiceImpl_());
        smService.sendMessage("天气过热，注意防暑！！！！");
    }
}
