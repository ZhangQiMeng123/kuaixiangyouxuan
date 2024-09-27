package com.hmdp.静态代理;

/**
 * 真实对象
 */
public class SmServiceImpl implements SmService{
    @Override
    public String sendMessage(String message) {
        System.out.println("message:"+message);
        return message;
    }
}
