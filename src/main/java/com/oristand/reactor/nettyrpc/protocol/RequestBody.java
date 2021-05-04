package com.oristand.reactor.nettyrpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lixiaoxuan
 * @description: TODO
 * @date 2021/5/4 13:42
 */
@Data
public class RequestBody implements Serializable {

    // 类名
    private String className;

    // 方法名
    private String methodName;

    // 参数类型
    private Class<?>[] paramsType;

    // 参数
    private Object[] args;
}
