package com.viscu.rpc.server;

import com.viscu.rpc.protocol.RpcDecoder;
import com.viscu.rpc.protocol.RpcEncoder;
import com.viscu.rpc.protocol.RpcRequest;
import com.viscu.rpc.protocol.RpcResponse;
import com.viscu.rpc.registry.RpcServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ rpc 服务端
 */

public class RpcServer implements ApplicationContextAware, InitializingBean{

    private static Logger logger = LoggerFactory.getLogger(RpcServer.class);

    /*rpc服务端监听的地址*/
    private String serverAddress;

    /*用于进行服务的注册*/
    private RpcServiceRegistry serviceRegistry;

    /*存放接口名与服务对象之间的映射关系*/
    private final Map<String, Object> handlerMap = new HashMap<>();

    private static ThreadPoolExecutor threadPoolExecutor;

    /*用于服务端接受客户端的连接*/
    private EventLoopGroup bossGroup = null;

    /*用于进行socketChannel的读写*/
    private EventLoopGroup workerGroup = null;

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, RpcServiceRegistry serviceRegistry) {
        this(serverAddress);
        this.serviceRegistry = serviceRegistry;
    }

    public static void submit(Runnable task){
        /*double check --singleton*/
        if(threadPoolExecutor == null){
            synchronized (RpcServer.class){
                if(threadPoolExecutor == null){
                    /*线程池创建 参数不做解释*/
                    threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /*启动rpc netty服务器*/
        start();
    }

    /*用于该服务器添加新的服务实现*/
    public RpcServer addService(String interfaceName, Object serviceBean){
        if(!handlerMap.containsKey(interfaceName)){
            logger.info("loading service: {}", interfaceName);
            handlerMap.put(interfaceName, serviceBean);
        }
        return this;
    }

    public void start() throws InterruptedException {
        if(workerGroup == null && bossGroup == null){
            try {
                bossGroup = new NioEventLoopGroup(1);
                workerGroup = new NioEventLoopGroup(30);
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                            /*第一个处理TCP拆包粘包问题 第二个rpc服务器编码器 第三个rpc服务器解码器 第四个是主要的业务处理*/
                                ch.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                        .addLast(new RpcEncoder(RpcResponse.class))
                                        .addLast(new RpcDecoder(RpcRequest.class))
                                        .addLast(new RpcHandler(handlerMap));
                            }
                        } )
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                String[] param = serverAddress.split(":");
                String host = param[0];
                int port = Integer.parseInt(param[1]);
            /*同步绑定 host:port 进行监听*/
                ChannelFuture future = serverBootstrap.bind(host, port).sync();
                logger.info("server started on port {}", port);
                if(serviceRegistry != null){
                /*将netty服务端服务地址注册到zookeeper上面 供客户端调用服务端的服务*/
                    serviceRegistry.register(serverAddress);
                }
                future.channel().closeFuture().sync();
            }finally {
                stop();
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        /*获取所有带有RpcService注解的 Spring Bean 即客户端调用的api对应的具体接口实现类*/
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for (Object serviceBean : serviceBeanMap.values()){
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                logger.info("服务的名称: {}", interfaceName);
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }

    /*关闭两个线程组*/
    public void stop(){
         /*优雅地进行关闭*/
        if(bossGroup != null){
            bossGroup.shutdownGracefully();
        }
        if(workerGroup  != null){
            workerGroup.shutdownGracefully();
        }
    }
}
