package com.oristand.reactor.nettyrpc.provider.service.impl;

import com.oristand.reactor.nettyrpc.provider.service.RpcService;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/4 13:21
 */
public class RpcServiceImpl implements RpcService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        return a - b;
    }

    @Override
    public int mult(int a, int b) {
        return a * b;
    }

    @Override
    public int div(int a, int b) {
        return a / b;
    }
}
