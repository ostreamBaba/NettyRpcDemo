package com.viscu.rpc.registry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @ Create by ostreamBaba on 19-1-2
 * @ zk的相关配置
 */

//@Component
public class Constant {

    //@Value("${ZK_SESSION_TIMEOUT}")
    public static Integer ZK_SESSION_TIMEOUT = 50000;

    //@Value("${ZK_REGISTRY_PATH}")
    public static String ZK_REGISTRY_PATH = "/registry";

    //@Value("${ZK_DATA_PATH}")
    public static String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        Constant constant = (Constant) applicationContext.getBean("constant");
        System.out.println(constant);
    }



}
