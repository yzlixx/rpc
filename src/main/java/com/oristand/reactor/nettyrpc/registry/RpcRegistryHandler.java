package com.oristand.reactor.nettyrpc.registry;

import com.oristand.reactor.nettyrpc.protocol.RequestBody;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lixiaoxuan
 * @description: 服务handler
 * @date 2021/5/4 14:34
 */
@ChannelHandler.Sharable
public class RpcRegistryHandler extends ChannelInboundHandlerAdapter {

    // 所有服务
    public static ConcurrentHashMap<String, Class<?>> registryMap = new ConcurrentHashMap();

    // 所有服务类名
    private static List<String> classNames = new ArrayList<>();

    private static RpcRegistryHandler rpcRegistryHandler;

    static {
        rpcRegistryHandler = new RpcRegistryHandler();
        //扫描所有注册服务
        scannerClass("com.oristand.reactor.nettyrpc.provider.service.impl");
        doRegistey();
    }

    public static RpcRegistryHandler getInstance() {
        return rpcRegistryHandler;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //消息处理
        Object result = new Object();
        RequestBody requestBody = (RequestBody) msg;
        String ioThreadName = Thread.currentThread().getName();
        //使用netty的eventloop处理业务
        ctx.executor().execute(() -> {
            try {
                if (registryMap.containsKey(requestBody.getClassName())) {
                    Class<?> clazz = registryMap.get(requestBody.getClassName());
                    Method method = clazz.getDeclaredMethod(requestBody.getMethodName(), requestBody.getParamsType());
                    Object responseMsg = method.invoke(clazz.newInstance(), requestBody.getArgs());
                    String execThreadName = Thread.currentThread().getName();
                    String s = "io thread: " + ioThreadName + " exec thread: " + execThreadName + " from args:" + requestBody.getArgs()[0];
                    System.out.println("********"+s+"*******");
                    ctx.writeAndFlush(responseMsg);
                    ctx.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private static void scannerClass(String packageName) {
        URL url = rpcRegistryHandler.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scannerClass(packageName + "." + file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }

    }

    private static void doRegistey() {
        if (classNames.size() > 0) {
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);
                    Class<?> interfaceInfo = clazz.getInterfaces()[0];
                    registryMap.put(interfaceInfo.getName(), clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
