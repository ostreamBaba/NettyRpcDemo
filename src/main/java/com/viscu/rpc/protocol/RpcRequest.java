package com.viscu.rpc.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Arrays;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ rpc请求消息体
 */

@Data
public class RpcRequest {

    /*当前请求的id*/
    private String requestId;

    /*请求的类*/
    private String className;

    /*请求的方法名*/
    private String methodName;

    /*请求的参数类型*/
    private Class<?>[] paramTypes;

    /*请求参数的具体值*/
    private Object[] parameters;

    @JsonIgnore
    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramTypes=" + Arrays.toString( paramTypes ) +
                ", parameters=" + Arrays.toString( parameters ) +
                '}';
    }
}
