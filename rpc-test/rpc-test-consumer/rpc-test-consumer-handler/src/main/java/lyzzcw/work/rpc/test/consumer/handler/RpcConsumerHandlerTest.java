package lyzzcw.work.rpc.test.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.consumer.common.RpcConsumer;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeaderFactory;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;

import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 10:39
 * Description: No Description
 */
@Slf4j
public class RpcConsumerHandlerTest {
    public static void main(String[] args) throws Exception {
        RpcConsumer rpcConsumer = RpcConsumer.getInstance();
        RpcFuture future = rpcConsumer.sendRequest(getRpcRequestProtocol());
        log.info("received rpc response:{}", future.get());
        TimeUnit.SECONDS.sleep(2L);
        rpcConsumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol(){
        log.info("发送数据开始...");
        //模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<RpcRequest>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk", RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setClassName("lyzzcw.work.rpc.test.api.DemoService");
        request.setGroup("lzy");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"lzy",28});
        request.setParameterTypes(new Class[]{String.class,Integer.class});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        log.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
        return protocol;
    }
}
