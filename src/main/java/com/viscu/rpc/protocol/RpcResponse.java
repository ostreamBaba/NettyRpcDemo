package com.viscu.rpc.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ rpc响应类
 */

@Data
public class RpcResponse {

    private String requestId;

    /*code对应的消息*/
    private String error;

    /*返回的数据*/
    private Object data;

    @JsonIgnore
    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}
