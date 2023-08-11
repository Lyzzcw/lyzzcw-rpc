package lyzzcw.work.rpc.consumer;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.proxy.api.async.IAsyncObjectProxy;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;
import lyzzcw.work.rpc.test.api.DemoService;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/5 9:14
 * Description: No Description
 */
@Slf4j
public class RpcConsumerNativeTest {
    @Test
    public void test() throws InterruptedException {
        RpcClient client = new RpcClient(
                "127.0.0.1:8848",
                RpcConstants.REGISTRY_CENTER_NACOS,
                RpcConstants.SERVICE_LOAD_BALANCER_LEAST_CONNECTIONS,
                RpcConstants.PROXY_JAVASSIST,
                "1.0.0",
                "lzy",
                RpcConstants.SERIALIZATION_PROTOSTUFF,
                3000,
                false,
                false);
        DemoService demoService = client.create(DemoService.class);
        String result = demoService.hello("lzy", 29);
        log.info("result: " + result);
    }

    @Test
    public void test1() throws InterruptedException, ExecutionException {
        RpcClient client = new RpcClient(
                "127.0.0.1",
                RpcConstants.REGISTRY_CENTER_ZOOKEEPER,
                RpcConstants.SERVICE_LOAD_BALANCER_ROUND_ROBIN,
                RpcConstants.PROXY_JDK,
                "1.0.0",
                "lzy",
                "json",
                3000,
                false,
                false);
        IAsyncObjectProxy proxy = client.createAsync(DemoService.class);
        RpcFuture rpcFuture = proxy.call("hello", "lzy", new Integer(29));
        String result = (String) rpcFuture.get();
        log.info("result: " + result);
    }
}
