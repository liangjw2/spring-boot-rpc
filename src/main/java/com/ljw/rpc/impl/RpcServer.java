package com.ljw.rpc.impl;

import com.ljw.rpc.util.JsonDecoder;
import com.ljw.rpc.util.JsonEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RpcServer implements ApplicationContextAware, InitializingBean {
    // RPC服务实现容器
    private static Map<String, Object> rpcServices = new HashMap<>();//可暂时修改为public变量，测试该变量值变化情况
    //@Value("${rpc.server.port}")
    private  int port=9998;
    private final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    @Override
    //此方法可以改为向注册中心注册服务
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //这里的getBeansWithAnnotation方法实际上已经为每个bean产生一个实例了
        Map<String, Object> services = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (Map.Entry<String, Object> entry : services.entrySet()) {
            Object bean = entry.getValue();
            //这里打印的日志可以看出services（Map<String, Object>）中的值，Object只是bean的引用地址值
            logger.info("从applicationContext容器中取出key:{},value：{}", entry.getKey(),entry.getValue());
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            for (Class<?> inter : interfaces) {
                rpcServices.put(inter.getName(),  bean);
                logger.info("加载RPC服务接口名:{},实现bean为：{}", inter.getSimpleName(), bean);
            }
        }
        logger.info("加载RPC服务数量:{}", rpcServices.size());
        logger.info("RpcServer里的rpcServices引用{}", rpcServices);
    }

    @Override
    public void afterPropertiesSet() {
        start();
    }

    private void start(){
        new Thread(() -> {
            //监听一个端口只需要一个boss线程
            EventLoopGroup boss = new NioEventLoopGroup(1);
            //每个worker对应一个处理IO任务的selector，netty默认线程数为cpu核心数的2倍（最佳）
            EventLoopGroup worker = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(boss, worker)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new IdleStateHandler(0, 0, 60));
                                pipeline.addLast(new JsonDecoder());//服务端是先解报文
                                pipeline.addLast(new JsonEncoder());
                                pipeline.addLast(new RpcServerInboundHandler(rpcServices));
                            }
                        })
                        .channel(NioServerSocketChannel.class);
                ChannelFuture future = bootstrap.bind(port).sync();
                logger.info("RPC 服务器启动, 监听端口:" + port);
                future.channel().closeFuture().sync();
            }catch (Exception e){
                e.printStackTrace();
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            }
        }).start();

    }
}
