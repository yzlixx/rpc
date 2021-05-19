package com.oristand.reactor.nettyrpc.consumer.factory;

import com.oristand.reactor.nettyrpc.consumer.proxy.RpcConsumerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lixiaoxuan
 * @description: 获取NiosocketChannel 工厂方法
 * @date 2021/5/19 19:38
 */
public class ClientFactory {

    int DEFAULT_SIZE = 100;

    private static ClientFactory factory;

    static {
        factory = new ClientFactory();
    }

    public static ClientFactory getFactory() {
        return factory;
    }

    // 每一个连接 对应多个channel 通道
    ConcurrentHashMap<InetSocketAddress, ClientPool> pools = new ConcurrentHashMap<>();

    // 根据绑定的连接地址获取channel
    public synchronized NioSocketChannel getClient(InetSocketAddress address) {
        ClientPool clientPool = pools.get(address);
        if (clientPool == null) {
            // 第一次连接
            pools.putIfAbsent(address, new ClientPool(DEFAULT_SIZE));
            clientPool = pools.get(address);
        }

        // 随机获取连接池，自己定义负载均衡策略
        Random random = new Random();
        int rd = random.nextInt(DEFAULT_SIZE);

        // 判断连接池对应得channel是否有效
        if (clientPool.clients[rd] != null && clientPool.clients[rd].isActive()) {
            return clientPool.clients[rd];
        }

        // 创建新的channel

        synchronized (clientPool.lock[rd]) {
            return clientPool.clients[rd] = create(address);
        }
    }

    // 创建新的channel
    private NioSocketChannel create(InetSocketAddress address) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bs = new Bootstrap();
        ChannelFuture furture = bs.group(worker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {

                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        pipeline.addLast(new LengthFieldPrepender(4));
                        pipeline.addLast("encoder", new ObjectEncoder());
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast(new RpcConsumerHandler());
                    }
                }).connect(address);

        try {
            NioSocketChannel channel = (NioSocketChannel) furture.sync().channel();
            return channel;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
