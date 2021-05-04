package com.oristand.reactor.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/3 16:53
 */
public class NettyServer {
    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new NettyServer(9090).start();
    }

    private void start() throws Exception {
        NioEventLoopGroup  group = new NioEventLoopGroup();
        ServerBootstrap sbs = new ServerBootstrap();
        ChannelFuture bind = sbs.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel SocketChannel) throws Exception {
                        ChannelPipeline pipeline = SocketChannel.pipeline();
                        pipeline.addLast(new ServerHandler());
                    }
                }).bind();
        try {
            ChannelFuture f = bind.sync();
            System.out.println(NettyServer.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully().sync();
        }

    }
}
