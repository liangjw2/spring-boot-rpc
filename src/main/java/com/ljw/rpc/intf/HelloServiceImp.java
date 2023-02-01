package com.ljw.rpc.intf;

import com.ljw.iobus.HelloService;
import com.ljw.rpc.impl.RpcService;
import com.ljw.iobus.SayHelloIn;
import com.ljw.iobus.SayHelloOut;

@RpcService
public class HelloServiceImp implements HelloService {
    @Override
    public SayHelloOut sayHello(SayHelloIn Input,String name) {
        SayHelloOut Output = new SayHelloOut();
        Output.setReply("Hello and Hi " + Input.getName()+ " "+name);
        return Output;
    }

}
