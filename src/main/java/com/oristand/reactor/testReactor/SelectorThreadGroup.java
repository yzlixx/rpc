package com.oristand.reactor.testReactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/4/26 15:50
 */
public class SelectorThreadGroup {

    SelectorThread[] selectorThreadsPool;

    AtomicInteger xid = new AtomicInteger(0);

    SelectorThreadGroup worker = this;

    public SelectorThreadGroup(int poolSize) {
        selectorThreadsPool = new SelectorThread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            selectorThreadsPool[i] = new SelectorThread(this);
            Thread thread = new Thread(selectorThreadsPool[i]);
//            thread.setName("线程...." + i);
            thread.start();
        }
    }

    public void bind(int port) {
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            nextSelector(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextSelector(Channel channel) {
        try {

            if (channel instanceof ServerSocketChannel) {
                SelectorThread selectorThread = next();
                selectorThread.lbq.put(channel);
                selectorThread.selector.wakeup();
            } else if (channel instanceof SocketChannel) {
                SelectorThread selectorThread = nextWorker();
                selectorThread.lbq.put(channel);
                selectorThread.selector.wakeup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SelectorThread next() {
        int index = xid.incrementAndGet() % selectorThreadsPool.length;
        return selectorThreadsPool[index];
    }

    public void setWorker(SelectorThreadGroup worker) {
        this.worker = worker;
    }

    public SelectorThread nextWorker() {
        int index = xid.incrementAndGet() % worker.selectorThreadsPool.length;
        return  worker.selectorThreadsPool[index];
    }
}
