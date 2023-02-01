package com.ljw.rpc.util;

import com.alibaba.fastjson.JSON;
import com.ljw.rpc.vo.RpcRequest;
import com.ljw.rpc.vo.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 将 RpcResponse 编码成字节序列发送（与客户端不同）
 * 消息格式: Length + Content
 * Length使用int存储,标识消息体的长度
 *
 * +--------+----------------+
 * | Length |  Content       |
 * |  4字节 |   Length个字节  |
 * +--------+----------------+
 */

public class JsonEncoder extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcResponse rpcResponse, ByteBuf out){
        byte[] bytes = JSON.toJSONBytes(rpcResponse);
        // 将消息体的长度写入消息头部
        out.writeInt(bytes.length);
        // 写入消息体
        out.writeBytes(bytes);
    }
}
