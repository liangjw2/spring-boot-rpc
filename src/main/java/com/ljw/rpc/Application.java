package com.ljw.rpc;

import com.ljw.rpc.impl.RpcService;
import com.ljw.rpc.intf.HelloServiceImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



//@SpringBootApplication
public class Application {

    private static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        //SpringApplication.run(Application.class, args);
        //这里只是试下读实现bean里的注解，不影响其他
        //Class clazz = HelloServiceImp.class;
        //System.out.println(clazz.getAnnotations());
        //System.out.println(clazz.getAnnotation(RpcService.class).toString());
        Initialize.init();



    }
}
