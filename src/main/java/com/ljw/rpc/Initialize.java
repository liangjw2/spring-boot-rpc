package com.ljw.rpc;

import com.ljw.rpc.config.MainConfigOfLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Initialize {

    private static final Logger logger = LoggerFactory.getLogger(Initialize.class);

    public static void init(){

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MainConfigOfLifeCycle.class);
        logger.info("初始化rpc服务端applicationContext……");
        //RpcServer rpcserver = applicationContext.getBean(RpcServer.class);
        //HelloService helloservice = applicationContext.getBean(HelloService.class);
        //logger.info("rpcserver里的rpcServices引用{}",rpcserver.rpcServices);
        //logger.info(helloservice.sayHello("test"));
        //可以在这里初始化一个全局变量区(相当于一个静态VO，成员变量和get/set方法皆为static)，将applicationContext写进全局变量区以方便各处代码getBean
        /*？运行变量区应该在交易控制层初始化，运行变量区相当于交易主控类的非静态成员变量，同时提供对运行变量区里每个要素的get/set方法；
           同时要将运行变量传给交易主控实例调用的其他方法，非常麻烦*/
        /*实现运行变量区，要利用java的ThreadLocal机制：在全局变量VO类上，增加一个用ThreadLocal修饰的静态成员变量，用来定义运行变量区；这样每个线程访问该静态变量时，
        就会获得一个单独的本地变量副本*//*可以在运行变量区定义一些数据结构来记录交易运行情况，例如栈、队列*/
        /*微服务架构下，是否要在各微服务之间传递当前运行变量区的值？*/

    }
}
