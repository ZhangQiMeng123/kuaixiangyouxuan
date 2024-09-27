package com.hmdp.静态代理;

public class Proxy {
    public static void main(String[] args) {
        SmsProxy smsProxy = new SmsProxy(new SmServiceImpl());
        smsProxy.sendMessage("天气温度过高，注意防暑!!!");
    }
}
