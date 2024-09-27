package com.hmdp.静态代理;

/**
 * 静态代理对象
 */
public class SmsProxy implements SmService{
    private SmService smService;

    public SmsProxy(SmService smService) {
        this.smService = smService;
    }

    @Override
    public String sendMessage(String message) {
        //被代理的方法执行前加入自己的逻辑
        System.out.println("被代理方法执行前.....");
        String result = smService.sendMessage(message);
        //被代理的方法执行后加入自己的逻辑
        System.out.println("被代理方法执行后.....");
        return result;
    }
}
