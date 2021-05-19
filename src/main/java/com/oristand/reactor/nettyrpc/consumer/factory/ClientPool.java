package com.oristand.reactor.nettyrpc.consumer.factory;

import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author lixiaoxuan
 * @description: NioSocketChannel 连接池
 * @date 2021/5/19 19:40
 */
public class ClientPool {
    NioSocketChannel[] clients;
    // 伴生锁
    Object[] lock;

    public ClientPool(int poolSize) {
        clients = new NioSocketChannel[poolSize];
        lock = new Object[poolSize];
        // 初始化锁
        for (int i = 0; i < poolSize; i++) {
            lock[i] = new Object();
        }
    }
}
