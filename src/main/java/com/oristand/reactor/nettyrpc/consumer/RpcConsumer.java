package com.oristand.reactor.nettyrpc.consumer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oristand.reactor.nettyrpc.consumer.proxy.RpcProxy;
import com.oristand.reactor.nettyrpc.provider.service.RpcHelloService;
import com.oristand.reactor.nettyrpc.provider.service.RpcService;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/4 15:07
 */
public class RpcConsumer {

    public static void main(String[] args) {
        RpcHelloService rpcHello = RpcProxy.getRpcProxy(RpcHelloService.class);

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();
        ExecutorService singleThreadPool = new ThreadPoolExecutor(4, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        int threadSize = 100;
        AtomicInteger num = new AtomicInteger(0);
        for (int i = 0; i < threadSize; i++) {
            singleThreadPool.execute(() -> {
                rpcHello.hello("this thread name:" + Thread.currentThread().getName() + ",lixiaoxuan,@" + num.incrementAndGet());
            });
        }
        singleThreadPool.shutdown();


//        RpcService rpcService = RpcProxy.getRpcProxy(RpcService.class);
//
//        System.out.println(rpcService.add(3,4));
//
//        System.out.println(rpcService.sub(3,4));
//
//        System.out.println(rpcService.mult(3,4));
//
//        System.out.println(rpcService.div(12,4));

    }
}
