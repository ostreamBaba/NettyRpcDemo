package com.viscu.rpc.client.demo;

import com.viscu.rpc.protocol.RpcDecoder;
import com.viscu.rpc.protocol.RpcEncoder;
import com.viscu.rpc.protocol.RpcRequest;
import com.viscu.rpc.protocol.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;

/**
 * @ Create by ostreamBaba on 19-1-6
 * @ rpc客户端
 */

public class RpcClientDemo extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientDemo.class);

    private String host;

    private int port;

    private RpcResponse response;

    private CountDownLatch latch;

    public RpcClientDemo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /*获取服务端的回应 并唤醒执行send的线程*/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        this.response = response;
        LOGGER.info("接受到消息: {}", response);
        latch.countDown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }


    /*通过netty客户端来发送消息*/
    public RpcResponse send(RpcRequest request) throws InterruptedException, BrokenBarrierException {
        latch = new CountDownLatch(1);
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel( NioSocketChannel.class)
                    .handler( new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(RpcClientDemo.this);
                        }
                    } ).option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(request).sync();
            /*等待接收到服务端的消息才进行下一步*/
            latch.await();
            if(response != null){
                /*关闭当前客户端*/
                future.channel().closeFuture().sync();
            }
            return response;
        }finally {
            group.shutdownGracefully();
        }
    }
}
