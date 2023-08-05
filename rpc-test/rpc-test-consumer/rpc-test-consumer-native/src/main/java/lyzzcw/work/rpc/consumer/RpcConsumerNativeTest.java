package lyzzcw.work.rpc.consumer;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.test.api.DemoService;
import org.junit.Test;

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
}
