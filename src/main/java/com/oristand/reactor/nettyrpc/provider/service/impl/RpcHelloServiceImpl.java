package com.oristand.reactor.nettyrpc.provider.service.impl;

import com.oristand.reactor.nettyrpc.provider.service.RpcHelloService;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/4 13:20
 */
public class RpcHelloServiceImpl implements RpcHelloService {
    @Override
    public String hello(String msg) {
        return "hello," + msg;
    }
}
