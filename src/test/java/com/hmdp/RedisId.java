package com.hmdp;

import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class RedisId {
    @Autowired
    private RedisIdWorker redisIdWorker;
    //定义300个线程池
    private ExecutorService es= Executors.newFixedThreadPool(300);
    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(300);
        //定义任务 使用lambda表达式
        Runnable task=()->{
            for (int i = 0; i <100 ; i++) {  //一个任务生成100个id
                long id=redisIdWorker.nextId("order");
                System.out.println("id="+id);
            }
            countDownLatch.countDown();
        };
        //每个线程提交一次任务，每个任务生成100个id，共生成30000个id
        long begin=System.currentTimeMillis();
        for (int i = 0; i <300 ; i++) {
            es.submit(task);
        }
        countDownLatch.await();
        long end=System.currentTimeMillis();
        System.out.println("time="+(end-begin));
    }
}
