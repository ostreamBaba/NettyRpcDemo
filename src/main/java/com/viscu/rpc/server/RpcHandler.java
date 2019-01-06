package com.viscu.rpc.server;

import com.viscu.rpc.protocol.RpcRequest;
import com.viscu.rpc.protocol.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ rpc handler处理客户端的请求
 */

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest>{

    private static Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("已有客户端连接该服务器...准备进行服务...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcServer.submit(()-> {
            logger.info( "receive request: " + request.getRequestId() );
            RpcResponse rpcResponse = new RpcResponse();
            try {
                Object result = null;
                result = handler(request);
                rpcResponse.setData(result);
            } catch (Exception e) {
                rpcResponse.setError( e.getMessage() );
                logger.debug( "rpc服务器处理出现异常: {}", e.getMessage() );
            }
            /*发送往后关闭连接*/
            ctx.writeAndFlush( rpcResponse ).addListener(ChannelFutureListener.CLOSE);
            /*ctx.writeAndFlush(rpcResponse).addListener((ChannelFutureListener)future->
                    logger.debug("发送响应消息: {}", request.getRequestId()));
            });*/
        });
    }

    /*提取必要的参数 类名 调用的类方法进行反射*/
    private Object handler(RpcRequest request) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        String className = request.getClassName();
        if(!handlerMap.containsKey(className)){
            logger.error("该服务尚未被注册");
            return null;
        }
        Object serviceBean = handlerMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();

        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        Class<?>[] paramTypes = request.getParamTypes();

        logger.debug("调用服务的类名: {}", serviceClass.getName());
        logger.debug("调用服务的方法名: {}", methodName);
        logger.debug("调用的参数类型为: ");
        for (int i = 0; i < paramTypes.length; i++) {
            logger.debug(paramTypes[i].getName());
        }
        logger.debug("调用的参数值为: ");
        for (int i = 0; i < parameters.length; i++) {
            logger.debug(parameters[i].toString());
        }

        /*
            利用反射
            Method method = serviceClass.getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(serviceBean, parameters);
        */

        /*cglib动态代理 性能更佳*/
        FastClass serviceFastClass = FastClass.create(serviceClass);
        int methodIndex = serviceFastClass.getIndex(methodName, paramTypes);
        return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务器发生异常: {}",cause);
        ctx.close();
    }
}
