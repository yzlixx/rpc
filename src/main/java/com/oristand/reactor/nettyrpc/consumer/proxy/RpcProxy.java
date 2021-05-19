package com.oristand.reactor.nettyrpc.consumer.proxy;

import com.oristand.reactor.nettyrpc.consumer.factory.ClientFactory;
import com.oristand.reactor.nettyrpc.protocol.RequestBody;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author lixiaoxuan
 * @description: 动态代理
 * @date 2021/5/4 13:37
 */
public class RpcProxy {

    public static <T> T getRpcProxy(Class<T> clazz) {

        ClassLoader classLoader = clazz.getClassLoader();

        Class<?>[] interfaces = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();

        MethodProxy methodProxy = new MethodProxy(clazz);

        return (T) Proxy.newProxyInstance(classLoader, interfaces, methodProxy);
    }

    public static class MethodProxy implements InvocationHandler {


        private Class<?> clazz;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            //
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            } else {
                // rpc调用方法
                return rpcInvoke(proxy, method, args);
            }
        }

        /**
         * @description: rpc调用
         * @author lixiaoxuan
         * @date 2021/5/4 15:14
         */
        private Object rpcInvoke(Object proxy, Method method, Object[] args) {

            // 1.处理自定义协议
            RequestBody body = new RequestBody();
            body.setArgs(args);
            body.setClassName(this.clazz.getName());
            body.setMethodName(method.getName());
            body.setParamsType(method.getParameterTypes());

            // 2.连接池获取 channel
            ClientFactory factory = ClientFactory.getFactory();
            NioSocketChannel channel = factory.getClient(new InetSocketAddress("localhost", 8080));
            try {
                channel.writeAndFlush(body).sync();
                channel.closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }

            RpcConsumerHandler rpcConsumerHandler = new RpcConsumerHandler();
            return rpcConsumerHandler.getResponse();
        }
    }

}
