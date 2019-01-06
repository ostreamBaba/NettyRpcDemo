package com.viscu.rpc.server;


import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ rpc服务注解 该注解具备被Spring扫描的能力
 * @ 用于扫描api对应的具体服务实现类
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService{
    Class<?> value();
}
