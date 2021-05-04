package com.oristand.reactor.nettyrpc.provider.service;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/4 13:16
 */
public interface RpcService {

    //加
    public int add(int a, int b);

    //减
    public int sub(int a, int b);

    //乘
    public int mult(int a, int b);

    //除
    public int div(int a, int b);
}
