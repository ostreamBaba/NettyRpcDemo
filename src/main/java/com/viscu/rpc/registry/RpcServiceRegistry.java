package com.viscu.rpc.registry;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @ Create by ostreamBaba on 19-1-2
 * @ 服务注册类
 * @ 注册中心zookeeper
 */

/*使用zookeeper可以轻松实现服务注册功能*/
public class RpcServiceRegistry{

    private final Logger logger = LoggerFactory.getLogger(RpcServiceRegistry.class);

    private String zkServerPath;

    private CuratorFramework client = null;

    /*客户端连接重试的次数 默认3次*/
    private int retryTimes = 3;

    /*每次重试的间隔时间 默认5000ms*/
    private int sleepMsBetweenRetries = 5000;

    public RpcServiceRegistry(String... zkServerPath) {
        StringBuilder buffer = new StringBuilder();
        for (String path : zkServerPath){
            buffer.append(path).append(",");
        }
        this.zkServerPath = buffer.substring(0, buffer.length() - 1);
    }

    public RpcServiceRegistry(int retryTimes, int sleepMsBetweenRetries, String... zkServerPath) {
        this(zkServerPath);
        this.retryTimes = retryTimes;
        this.sleepMsBetweenRetries = sleepMsBetweenRetries;
    }

    /**
     * @描述 对zk进行初始化配置 同步创建zk 如果使用原生api的话是异步创建 这里需要区分一下
     * @return void
     * @create by ostreamBaba on 下午4:48 19-1-2
     */

    public void create(){
        try {
            RetryPolicy retryPolicy = new RetryNTimes(retryTimes, sleepMsBetweenRetries);
            //实例化客户端
            client = CuratorFrameworkFactory.builder()
                    .connectString(zkServerPath)
                    .sessionTimeoutMs(Constant.ZK_SESSION_TIMEOUT)
                    .retryPolicy(retryPolicy)
                    .namespace("rpc_workspace")
                    .build();
            /*启动*/
            client.start();
        }catch (Exception e){
            logger.error("客户端连接zk服务器发生异常, 请重试...");
        }
    }

    private void init(){
        try{
            /*判断zk的父亲节点是否被创建*/
            /*设置为持久化节点 权限为任何人都可以访问*/
            if(client.checkExists().forPath(Constant.ZK_REGISTRY_PATH) == null){
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(Constant.ZK_REGISTRY_PATH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @描述 进行服务的注册
     * @param data 节点数据
     * @return void
     * @create by ostreamBaba on 下午5:11 19-1-2
     */

    public void register(String data){
        if(StringUtils.isBlank(data)){
            return;
        }
        /*客户端还没有实例化*/
        if(client == null){
            create();
        }
        /*查看是否父亲节点已经建立*/
        init();
        /*创建子节点*/
        /*
            zk工作目录为
            |—— /rpc_workspace
                    |—— /registry(持久节点 用于存放所有服务器临时节点)
                             |—— /data(临时节点 存放数据为rpc服务地址(host:port))
         */
        createNode(data);
    }


    /**
     * @描述 创建子节点
     * @param data 设置子节点的数据
     * @return void
     * @create by ostreamBaba on 下午7:03 19-1-2
     */

    private void createNode(String data) {
        try {
            /*若没有创建 则创建子节点并设置子节点的数据*/
            if(client.checkExists().forPath(Constant.ZK_DATA_PATH) == null){
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(Constant.ZK_DATA_PATH, data.getBytes());
            }else {
                /*创建了的话 直接设置数据(不采用乐观锁)*/
                client.setData()
                        .forPath(Constant.ZK_DATA_PATH, data.getBytes());
            }
        }catch (Exception e){
            logger.error("创建节点失败");
        }
    }

    /**
     * @描述 关闭客户端
     * @return void
     * @create by ostreamBaba on 下午7:12 19-1-2
     */

    public void closeClient(){
        if(client != null){
            try {
                client.close();
            }catch (Exception e){
                logger.error("客户端关闭失败...");
            }
        }
    }

    public String getZkServerPath() {
        return zkServerPath;
    }

}
