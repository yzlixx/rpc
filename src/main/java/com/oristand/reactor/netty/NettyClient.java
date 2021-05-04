package com.oristand.reactor.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/3 17:07
 */
public class NettyClient {
    private final String host;
    private final int port;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        NettyClient nettyClient = new NettyClient("192.168.10.215", 9090);
        nettyClient.start();
    }

    private void start() throws Exception {
        NioEventLoopGroup group  = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture connect = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new ClientHandler());
                    }
                }).connect();

        try {
            ChannelFuture f = connect.sync();
            Scanner sc = new Scanner(System.in);
            InputStreamReader is = new InputStreamReader(System.in);
            Channel channel = f.channel();
            channel.writeAndFlush(is);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully().sync();
        }

    }
}
