package com.ljw.rpc.util;

import com.alibaba.fastjson.JSON;
import com.ljw.rpc.vo.RpcRequest;
import com.ljw.rpc.vo.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 将请求消息解码成 RpcRequest（与客户端不同）
 */
public class JsonDecoder extends LengthFieldBasedFrameDecoder {

    public JsonDecoder(){
        super(Integer.MAX_VALUE, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf msg = (ByteBuf) super.decode(ctx, in);
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        RpcRequest rpcRequest = JSON.parseObject(bytes, RpcRequest.class);
        return rpcRequest;
    }
}
