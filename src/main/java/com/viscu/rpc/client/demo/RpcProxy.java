package com.viscu.rpc.client.demo;

import com.viscu.rpc.protocol.RpcRequest;
import com.viscu.rpc.protocol.RpcResponse;
import com.viscu.rpc.registry.RpcServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @ Create by ostreamBaba on 19-1-6
 * @ 实现rpc代理 通过rpc动态代理来调用netty客户端发送消给服务端
 */

public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serverAddress;

    private RpcServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(RpcServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /*使用jdk动态代理*/
    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass}, (proxy, method, param) -> {
                    /*创建并初始化 RPC请求*/
                    LOGGER.debug("代理类: {}", proxy.getClass().getName());
                    RpcRequest request = new RpcRequest();
                    request.setRequestId(UUID.randomUUID().toString());
                    request.setClassName(method.getDeclaringClass().getName());
                    request.setMethodName(method.getName());
                    request.setParamTypes(method.getParameterTypes());
                    request.setParameters(param);

                    if(serviceDiscovery != null){
                        /*发现服务*/
                        serverAddress = serviceDiscovery.discover();
                    }

                    /*服务发现的远程地址*/
                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);

                    /*创建rpc客户端 通过客户端与rpc服务端进行交互获得需要的数据*/
                    RpcClientDemo client = new RpcClientDemo(host, port);
                    /*发送数据*/
                    RpcResponse response = client.send(request);

                    LOGGER.debug("接受到的消息: {}", response);
                    if(response.getError() != null){
                        throw new RuntimeException();
                    }else {
                        return response.getData();
                    }
                } );
    }

}
