package com.oristand.reactor.testReactor;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/4/26 16:34
 */
public class MainThread {

    public static void main(String[] args) throws InterruptedException {
        SelectorThreadGroup boss = new SelectorThreadGroup(3);

        SelectorThreadGroup work = new SelectorThreadGroup(3);
        boss.setWorker(work);
        boss.bind(9090);
        boss.bind(8888);
        boss.bind(7777);
    }
}
