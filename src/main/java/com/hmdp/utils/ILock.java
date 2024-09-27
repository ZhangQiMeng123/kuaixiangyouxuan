package com.hmdp.utils;

/**
 * 分布式锁接口
 */
public interface ILock {
    //尝试获取锁
    boolean tryLock(long timeoutSec);
    //释放锁
    void unLock();
}
