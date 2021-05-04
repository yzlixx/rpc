package com.oristand.reactor.nettyrpc.consumer;

import com.oristand.reactor.nettyrpc.consumer.proxy.RpcProxy;
import com.oristand.reactor.nettyrpc.provider.service.RpcHelloService;
import com.oristand.reactor.nettyrpc.provider.service.RpcService;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/4 15:07
 */
public class RpcConsumer {

    public static void main(String[] args) {
        RpcHelloService rpcHello = RpcProxy.getRpcProxy(RpcHelloService.class);

        System.out.println(rpcHello.hello("lixiaoxuan"));

        RpcService rpcService = RpcProxy.getRpcProxy(RpcService.class);

        System.out.println(rpcService.add(3,4));

        System.out.println(rpcService.sub(3,4));

        System.out.println(rpcService.mult(3,4));

        System.out.println(rpcService.div(12,4));

    }
}
