package com.viscu.nettyrpc.test.client;

import com.viscu.rpc.client.demo.RpcProxy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @ Create by ostreamBaba on 19-1-5
 * @ 测试
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class ServerTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void test(){
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("world");
        Assert.assertEquals("hello world", result);
    }

}
