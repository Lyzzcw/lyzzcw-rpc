package lyzzcw.work.rpc.test.registry;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.common.helper.RpcServiceHelper;
import lyzzcw.work.rpc.protocol.meta.ServiceMeta;
import lyzzcw.work.rpc.registry.api.RegistryService;
import lyzzcw.work.rpc.registry.api.config.RegistryConfig;
import lyzzcw.work.rpc.registry.zookeeper.ZookeeperRegistryService;
import lyzzcw.work.rpc.test.api.DemoService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/5 13:56
 * Description: No Description
 */
@Slf4j
public class ZookeeperRegistryTest {
    private RegistryService registryService;
    private ServiceMeta serviceMeta;
    @Before
    public void init() throws Exception{
        RegistryConfig registryConfig = new RegistryConfig("127.0.0.1:2181", "zookeeper","random");
        this.registryService = new ZookeeperRegistryService();
        this.registryService.init(registryConfig);
        this.serviceMeta = new ServiceMeta(DemoService.class.getName(), "1.0.0", "127.0.0.1", 8080,"lzy",1);
    }
    @Test
    public void testRegister() throws Exception {
        this.registryService.register(serviceMeta);
        TimeUnit.SECONDS.sleep(20);
    }
    @Test
    public void testUnRegister() throws Exception {
        this.registryService.unRegister(serviceMeta);
    }
    @Test
    public void testDiscovery() throws Exception {
        String serverName = RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup());
        ServiceMeta discovery =  this.registryService.discovery(serverName, "lzy".hashCode(),null);
        log.info("discovery: " + discovery);
    }
    @Test
    public void testDestroy() throws Exception {
        this.registryService.destroy();
    }
}
