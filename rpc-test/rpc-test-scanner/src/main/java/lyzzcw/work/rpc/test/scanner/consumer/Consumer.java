package lyzzcw.work.rpc.test.scanner.consumer;

import lyzzcw.work.rpc.annotation.RpcReference;
import lyzzcw.work.rpc.test.api.DemoService;


/**
 * @author lzy
 * @version 1.0
 * Date: 2023/7/25 9:43
 * Description: No Description
 */
public class Consumer {
    @RpcReference(registryType = "zookeeper", registryAddress = "127.0.0.1:2181", version = "1.0.0", group = "lyzzcw")
    private DemoService demoService;
}
