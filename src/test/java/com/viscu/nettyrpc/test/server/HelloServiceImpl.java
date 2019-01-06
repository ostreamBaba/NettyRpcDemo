package com.viscu.nettyrpc.test.server;

import com.viscu.nettyrpc.test.client.HelloService;
import com.viscu.rpc.server.RpcService;

/**
 * @ Create by ostreamBaba on 19-1-6
 */

/*
* 服务端扫描该端口
* 指定远程接口
*/
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService{

    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
