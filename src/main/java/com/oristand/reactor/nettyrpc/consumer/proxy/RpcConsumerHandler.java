package com.oristand.reactor.nettyrpc.consumer.proxy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/4 15:23
 */
@ChannelHandler.Sharable
public class RpcConsumerHandler extends ChannelInboundHandlerAdapter {

    private Object response;

    public Object getResponse(){
        return response;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        response = msg;
        System.out.println("client receive, "+response.toString());
    }
}
