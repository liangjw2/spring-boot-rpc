package com.ljw.rpc.client;

import com.ljw.rpc.impl.RpcServer;
import com.ljw.rpc.util.JsonDecoder;
import com.ljw.rpc.util.JsonEncoder;
import com.ljw.rpc.vo.RpcRequest;
import com.ljw.rpc.vo.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * RPC远程调用的客户端
 */
@Slf4j
@Component
public class RpcClient {

    //@Value("${rpc.remote.ip}")
    private String remoteIp="localhost";

    //@Value("${rpc.remote.port}")
    private int port=9998;

    private Bootstrap bootstrap;
    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    // 储存调用结果
    private final Map<String, SynchronousQueue<RpcResponse>> results = new ConcurrentHashMap<>();

    public RpcClient(){

    }

    @PostConstruct
    public void init(){
        bootstrap = new Bootstrap().remoteAddress(remoteIp, port);
        NioEventLoopGroup worker = new NioEventLoopGroup(1);
        bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 10));
                        pipeline.addLast(new JsonEncoder());
                        pipeline.addLast(new JsonDecoder());
                        pipeline.addLast(new RpcClientInboundHandler(results));
                    }
                });
    }

    public RpcResponse send(RpcRequest rpcRequest) {
        RpcResponse rpcResponse = null;
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        Channel channel = null;
        try {
            channel = bootstrap.connect().sync().channel();
            logger.info("连接建立, 发送请求:{}", rpcRequest);
            channel.writeAndFlush(rpcRequest);
            SynchronousQueue<RpcResponse> queue = new SynchronousQueue<>();
            results.put(rpcRequest.getRequestId(), queue);
            // 阻塞等待获取响应
            rpcResponse = queue.take();
            results.remove(rpcRequest.getRequestId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(channel != null && channel.isActive()){
                channel.close();
            }
        }
        return rpcResponse;
    }
}
