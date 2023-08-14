package lyzzcw.work.rpc.consumer;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.consumer.common.RpcConsumer;
import lyzzcw.work.rpc.proxy.api.ProxyFactory;
import lyzzcw.work.rpc.proxy.api.async.IAsyncObjectProxy;
import lyzzcw.work.rpc.proxy.api.config.ProxyConfig;
import lyzzcw.work.rpc.proxy.api.object.ObjectProxy;
import lyzzcw.work.rpc.proxy.jdk.JdkProxyFactory;
import lyzzcw.work.rpc.registry.api.RegistryService;
import lyzzcw.work.rpc.registry.api.config.RegistryConfig;
import lyzzcw.work.rpc.registry.zookeeper.ZookeeperRegistryService;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/5 9:09
 * Description: 服务消费者RPC客户端
 */
@Slf4j
public class RpcClient {
    /**
     * 服务版本
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 序列化类型
     */
    private String serializationType;
    /**
     * 超时时间
     */
    private long timeout;
    /**
     * 服务注册与发现实例
     */
    private RegistryService registryService;
    /**
     * 是否异步调用
     */
    private boolean async;
    /**
     * 是否单向调用
     */
    private boolean oneway;
    /**
     * 动态代理方式
     */
    private String proxy;
    /**
     * 心跳间隔时间，默认30秒
     */
    private int heartbeatInterval;
    /**
     * 扫描空闲连接时间，默认60秒
     */
    private int scanNotActiveChannelInterval;
    public RpcClient(String registryAddress, String registryType,String registryLoadBalanceType,
                     String proxy, String serviceVersion, String serviceGroup,
                     String serializationType, long timeout, boolean async,
                     boolean oneway, int heartbeatInterval, int scanNotActiveChannelInterval) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = this.getRegistryService(registryAddress,
                registryType,registryLoadBalanceType);
        this.proxy = proxy;
        this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
    }

    /**
     * 创建服务注册与发现的实例
     */
    private RegistryService getRegistryService(String registryAddress,
                                               String registryType,
                                               String registryLoadBalanceType) {
        //TODO 后续拓展支持SPI
        RegistryService registryService = null;
        try {
            registryService = ExtensionLoader.getExtension(RegistryService.class,registryType);
            registryService.init(new RegistryConfig(registryAddress,
                    registryType,registryLoadBalanceType));
        } catch (Exception e) {
            log.error("registry service init error",e);
        }
        return registryService;
    }

    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class,proxy);
        proxyFactory.init(new ProxyConfig(interfaceClass,serviceVersion,
                serviceGroup, timeout,registryService,
                RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval),
                serializationType, async, oneway));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass, serviceVersion, serviceGroup,
                serializationType, timeout,registryService,
                RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval),
                async, oneway);
    }
    public void shutdown() {
        RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval).close();
    }
}
