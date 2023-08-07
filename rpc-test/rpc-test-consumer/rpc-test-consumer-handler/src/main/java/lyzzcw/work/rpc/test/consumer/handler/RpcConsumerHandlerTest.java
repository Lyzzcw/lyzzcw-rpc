package lyzzcw.work.rpc.test.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.consumer.common.RpcConsumer;
import lyzzcw.work.rpc.consumer.common.context.RpcContext;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeaderFactory;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.proxy.api.callback.AsyncRpcCallback;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;
import lyzzcw.work.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 10:39
 * Description: No Description
 */
@Slf4j
public class RpcConsumerHandlerTest {

    @Test
    public void testSync() throws Exception {
        RpcConsumer rpcConsumer = RpcConsumer.getInstance();
        RpcFuture future = rpcConsumer.sendRequest(getRpcRequestProtocol(),new ZookeeperRegistryService());
        future.addCallback(new AsyncRpcCallback() {
            @Override
            public void onSuccess(Object result) {
                log.info("received rpc response:{}", result);
            }

            @Override
            public void onException(Exception e) {
                log.info("received rpc response error:{}", e.getMessage());
            }
        });

        TimeUnit.SECONDS.sleep(2L);
        rpcConsumer.close();
    }

    @Test
    public void testAsync() throws Exception {
        RpcConsumer rpcConsumer = RpcConsumer.getInstance();
        rpcConsumer.sendRequest(getRpcRequestProtocol(),new ZookeeperRegistryService());
        RpcFuture future = RpcContext.getContext().getRpcFuture();
        log.info("received rpc response:{}", future.get());
        TimeUnit.SECONDS.sleep(2L);
        rpcConsumer.close();
    }

    @Test
    public void testOneway() throws Exception {
        RpcConsumer rpcConsumer = RpcConsumer.getInstance();
        rpcConsumer.sendRequest(getRpcRequestProtocol(),new ZookeeperRegistryService());
        log.info("oneway none need response");
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
        //set send type
        request.setAsync(false);
        request.setOneway(false);

        protocol.setBody(request);
        log.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
        return protocol;
    }


}
