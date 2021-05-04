package com.oristand.reactor.nettyrpc.consumer.proxy;

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


            NioEventLoopGroup group = new NioEventLoopGroup();
            final  RpcConsumerHandler rpcConsumerHandler = new RpcConsumerHandler();
            try {
                Bootstrap bs = new Bootstrap();
                ChannelFuture furture = bs.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY,true)
                        .handler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {

                                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                pipeline.addLast(new LengthFieldPrepender(4));
                                pipeline.addLast("encoder", new ObjectEncoder());
                                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                pipeline.addLast(rpcConsumerHandler);
                            }
                        }).connect("localhost", 8080);
                ChannelFuture f = furture.sync();
                f.channel().writeAndFlush(body).sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                group.shutdownGracefully();
            }


            return rpcConsumerHandler.getResponse();
        }
    }

}
