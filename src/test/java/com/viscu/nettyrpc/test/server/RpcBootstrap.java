package com.viscu.nettyrpc.test.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @ Create by ostreamBaba on 19-1-5
 * @ 启动服务器并发动服务
 */

public class RpcBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("server-spring.xml");
    }

}
