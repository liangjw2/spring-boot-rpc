package com.ljw.rpc.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ljw.rpc.vo.RpcRequest;
import com.ljw.rpc.vo.RpcResponse;
import com.ljw.iobus.SayHelloIn;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

//@Slf4j
public class RpcServerInboundHandler extends ChannelInboundHandlerAdapter {

    private Map<String, Object> rpcServices;
    private static final Logger logger = LoggerFactory.getLogger(RpcServerInboundHandler.class);

    public RpcServerInboundHandler(Map<String, Object> rpcServices){
        this.rpcServices = rpcServices;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端连接成功,{}", ctx.channel().remoteAddress());
        //rpcServices=null;/*此处测试是否影响rpcServices的值，发现Map<String, Object>中的Object只是对象的引用地址值*/
        logger.info("RpcServerInboundHandler里的rpcServices引用{}", rpcServices);
        //logger.info("RpcServer里的rpcServices引用{}", RpcServer.rpcServices);
    }

    public void channelInactive(ChannelHandlerContext ctx)   {
        logger.info("客户端断开连接,{}", ctx.channel().remoteAddress());
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        RpcRequest rpcRequest = (RpcRequest) msg;
        logger.info("接收到客户端请求, 请求接口:{}, 请求方法:{}", rpcRequest.getClassName(), rpcRequest.getMethodName());
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        Object result = null;
        try {
            result = this.handleRequest(rpcRequest);
            response.setResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
        }
        logger.info("服务器响应:{}", response);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("连接异常");
        ctx.channel().close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state()== IdleState.ALL_IDLE){
                logger.info("客户端已超过60秒未读写数据, 关闭连接.{}",ctx.channel().remoteAddress());
                ctx.channel().close();
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }

    private Object handleRequest(RpcRequest rpcRequest) throws Exception{
        Object bean = rpcServices.get(rpcRequest.getClassName());
        //如果要将此RPC框架提供给核心使用，这里则可以使用以下方式取bean:-----如此就不必用到Springboot的依赖注入了
        //同时，如果rpcRequest封装了一个环境运行变量（上下文），可以在这里取出来赋值到本地的环境运行变量
        //Class intfclazz=Class.forName(rpcRequest.getClassName());
        //Object bean = SysUtil.getInstance(intfclazz);
        if(bean == null){
            throw new RuntimeException("未找到对应的服务: " + rpcRequest.getClassName());
        }
        logger.info("参数类型{}",rpcRequest.getParameterTypes());//服务端的VO路径必须与客户端的VO路径一致，这里才能取到值
        logger.info("参数{}",rpcRequest.getParameters());
        logger.info("参数类型2{}",rpcRequest.getParameters()[0].getClass());
        Method method = bean.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        logger.info("方法{}",method.getName());

        Object[] paras = new Object[rpcRequest.getParameters().length];
        for(int i=0;i<rpcRequest.getParameters().length;i++) {
            //客户端的对象入参，经过序列化转为json字节流
            // 服务端将json字节流反序列化之后，rpcRequest.getParameters()中的对象入参变成了JSONObject，需要按其类型转换为Object
            if(rpcRequest.getParameters()[i] instanceof JSONObject) {
                paras[i] = JSONObject.toJavaObject((JSONObject) rpcRequest.getParameters()[i], rpcRequest.getParameterTypes()[i]);
            } else {
                paras[i] = rpcRequest.getParameters()[i];
            }
        }

        //事务开始，或此处嵌入分布式事务处理流程
        Object req = method.invoke(bean, paras);

        //此处传入的bean是个单实例
        /*如果需要使用多实例执行method.invoke（即每次请求使用不同的bean实例进行处理）,可以将接口实现类同时加上多实例模式的注解，
        但这里并不方便从applicationContext再次取bean，除非实现一个全局变量区，在applicationContext初始化后写入*/
        //也可以用以下方式实现多实例
        //return method.invoke(bean.getClass().newInstance(), rpcRequest.getParameters());
        /*通常来说，服务是面向接口设计，而不是面向对象设计，因此单例即可*/
        return req;
    }
}
