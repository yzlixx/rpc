package com.oristand.reactor.testReactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/4/26 15:48
 */
public class SelectorThread implements Runnable {

    Selector selector = null;
    SelectorThreadGroup selectorThreadGroup = null;
    LinkedBlockingQueue<Channel> lbq = new LinkedBlockingQueue<>();

    public  SelectorThread(SelectorThreadGroup stg) {
        try {
            selector = Selector.open();
            selectorThreadGroup = stg;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                //1.select();
                //阻塞的
//                System.out.println(Thread.currentThread().getName()+"   :  before select...."+ selector.keys().size());
                int nums = selector.select();
//                System.out.println(Thread.currentThread().getName()+"   :  after select...."+ selector.keys().size());
                if (nums > 0) {
                    //2.processSelectKeys
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                        iter.remove();
                    }
                }

                //3.runAllTasks
                if (!lbq.isEmpty()) {
                    Channel c = lbq.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName() + " register listen");
                    } else if (c instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                        client.register(selector, SelectionKey.OP_READ, buffer);
                        System.out.println(Thread.currentThread().getName() + " register client: " + client.getRemoteAddress());

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void acceptHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + "   acceptHandler......");
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            selectorThreadGroup.nextSelector(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName()+" read......");
        ByteBuffer buffer = (ByteBuffer)key.attachment();
        SocketChannel client = (SocketChannel)key.channel();
        buffer.clear();
        while(true){
            try {
                int num = client.read(buffer);
                if(num > 0){
                    buffer.flip();  //将读到的内容翻转，然后直接写出
                    while(buffer.hasRemaining()){
                        client.write(buffer);
                    }
                    buffer.clear();
                }else if(num == 0){
                    break;
                }else if(num < 0 ){
                    //客户端断开了
                    System.out.println("client: " + client.getRemoteAddress()+"closed......");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
