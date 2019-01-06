package com.viscu.rpc.protocol;

import com.viscu.rpc.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ rpc解码类
 */

public class RpcDecoder extends ByteToMessageDecoder{

    private Class<?> clazz;

    public RpcDecoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * HEAD_LENGTH表示消息头的长度(int型 4个字节)
     */

    private static final int HEAD_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < HEAD_LENGTH){
            return;
        }
        /*标记一下当前的readIndex的位置*/
        in.markReaderIndex();
        /*返回当前readerIndex处的int值，然后将readerIndex加4*/
        /*获取消息体的长度*/
        int dataLength = in.readInt();
        /*读取消息体长度为0*/
        if(dataLength <= 0){
            ctx.close();
        }
        /*读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex.
        这个配合markReaderIndex使用的。把readIndex重置到mark的地方*/
        if(in.readableBytes() < dataLength){
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        /*进行反序列化*/
        Object obj = SerializationUtil.deserialize(data, clazz);
        out.add(obj);
    }
}
