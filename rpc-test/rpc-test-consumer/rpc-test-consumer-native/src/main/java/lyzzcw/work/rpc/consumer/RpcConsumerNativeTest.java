package lyzzcw.work.rpc.consumer;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.proxy.api.async.IAsyncObjectProxy;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;
import lyzzcw.work.rpc.test.api.DemoService;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
        RpcClient client = new RpcClient("1.0.0","lzy",
                "jdk",3000,false,false);
        DemoService demoService = client.create(DemoService.class);
        String result = demoService.hello("lzy",29);
        log.info("result: " + result);
        TimeUnit.SECONDS.sleep(3L);
        client.shutdown();
    }

    @Test
    public void test1() throws InterruptedException, ExecutionException {
        RpcClient client = new RpcClient("1.0.0","lzy",
                "jdk",3000,false,false);
        IAsyncObjectProxy proxy = client.createAsync(DemoService.class);
        RpcFuture rpcFuture = proxy.call("hello","lzy", new Integer(29));
        String result = (String) rpcFuture.get();
        log.info("result: " + result);
        TimeUnit.SECONDS.sleep(3L);
        client.shutdown();
    }
}