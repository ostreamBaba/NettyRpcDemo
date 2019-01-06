package com.viscu.rpc.protocol;

import com.viscu.rpc.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ rpc编码类
 * @ 将消息分为两部分，消息头和消息尾，消息头中写入要发送数据的总长度，通常是在消息头的第一个字段使用int值来标识发送数据的长度
 */

public class RpcEncoder extends MessageToByteEncoder{

    private Class<?> clazz;

    public RpcEncoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        /*这个对象能不能被转化为这个类*/
        if(clazz.isInstance(in)){
            byte[] data = SerializationUtil.serialize(in);
            if(data != null){
                /*先将消息长度写入，也就是消息头*/
                out.writeInt(data.length);
                /*消息体中包含我们要发送的数据*/
                out.writeBytes(data);
            }
        }
    }
}
