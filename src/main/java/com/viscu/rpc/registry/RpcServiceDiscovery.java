package com.viscu.rpc.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ RPC服务检测
 */

public class RpcServiceDiscovery {

    private final Logger logger = LoggerFactory.getLogger( RpcServiceDiscovery.class);

    private String zkServerPath;

    private CuratorFramework client = null;

    private List<String> dataList = new ArrayList<>();

    /**
     * 客户端连接重试的次数 默认3次
     */
    private int retryTimes = 3;

    /**
     * 每次重试的间隔时间 默认5000ms
     */
    private int sleepMsBetweenRetries = 5000;

    public RpcServiceDiscovery(String... zkServerPath) {
        StringBuilder buffer = new StringBuilder();
        for (String path : zkServerPath){
            buffer.append(path).append(",");
        }
        this.zkServerPath = buffer.substring(0, buffer.length() - 1);
        //直接实例化客户端
        create();
    }

    public RpcServiceDiscovery(int retryTimes, int sleepMsBetweenRetries, String... zkServerPath) {
        this(zkServerPath);
        this.retryTimes = retryTimes;
        this.sleepMsBetweenRetries = sleepMsBetweenRetries;
    }

    private void create(){
        try {
            RetryPolicy retryPolicy = new RetryNTimes(retryTimes, sleepMsBetweenRetries);
            //实例化客户端
            client = CuratorFrameworkFactory.builder()
                    .connectString(zkServerPath)
                    .sessionTimeoutMs( Constant.ZK_SESSION_TIMEOUT)
                    .retryPolicy(retryPolicy)
                    .namespace("rpc_workspace")
                    .build();
            //对子节点进行监听
            addWatcher( Constant.ZK_REGISTRY_PATH);
        }catch (Exception e){
            logger.error("客户端连接zk服务器发生异常, 请重试...");
        }
    }

    /**
     * @描述 使用缓存来进行持久化的监听
     * @param path 父亲节点
     * @return void
     * @create by ostreamBaba on 下午7:39 19-1-2
     */

    private void addWatcher(String path) throws Exception {
        final PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener((client, event)->{
            if(event.getType().equals( PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                logger.info("节点数据更新");
                try {
                    nodeDataChanged(path);
                }catch (Exception e){
                    logger.error("发现异常: {}", e.getMessage());
                }
            }
        });
    }

    /**
     * @描述 节点数据发送变化 采取相关的处理
     * @param path
     * @return void
     * @create by ostreamBaba on 下午7:40 19-1-2
     */

    private void nodeDataChanged(String path) throws Exception {
        /*获取子节点的列表*/
        List<String> nodeList = client.getChildren().forPath(path);
        List<String> dataList = new ArrayList<>();
        /*获取子节点的数据*/
        nodeList.forEach((node)->{
            try {
                byte[] bytes = client.getData().forPath( Constant.ZK_REGISTRY_PATH + "/" + node);
                dataList.add(new String(bytes));
            } catch (Exception e) {
                logger.error("获取数据失败");
            }
        });
        logger.debug("节点的数据为: {}", dataList);
        this.dataList = dataList;

        logger.debug("rpc服务发现触发服务的服务器节点");
        updateConnectServer();
    }

    private void updateConnectServer() {

    }

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
