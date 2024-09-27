package com.hmdp.JDK动态代理;

import com.hmdp.静态代理.SmService;

/**
 * 真实对象
 */
public class SmServiceImpl_ implements SmService {
    @Override
    public String sendMessage(String message) {
        System.out.println("message:"+message);
        return message;
    }
}
