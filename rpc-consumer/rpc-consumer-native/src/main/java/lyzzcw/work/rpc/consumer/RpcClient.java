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
import lyzzcw.work.rpc.threadpool.ConcurrentThreadPool;

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
    /**
     * 重试间隔时间
     */
    private int retryInterval = 1000;
    /**
     * 重试次数
     */
    private int retryTimes = 3;
    /**
     * 是否开启结果缓存
     */
    private boolean enableResultCache;

    /**
     * 缓存结果的时长，单位是毫秒
     */
    private int resultCacheExpire;
    /**
     * 是否开启直连服务
     */
    private boolean enableDirectServer;
    /**
     * 直连服务的地址
     */
    private String directServerUrl;

    /**
     * 是否开启延迟连接(懒加载，需要时在发起连接)
     */
    private boolean enableDelayConnection;

    /**
     * 并发线程池
     */
    private ConcurrentThreadPool concurrentThreadPool;

    /**
     * 流控分析处理器
     */
    private String flowType;

    /**
     * 是否开启数据缓冲
     */
    private boolean enableBuffer;

    /**
     * 缓冲区大小
     */
    private int bufferSize;

    /**
     * 反射类型
     */
    private String reflectType;

    /**
     * 容错类
     */
    private Class<?> fallbackClass;


    public RpcClient(String registryAddress,
                     String registryType,
                     String loadBalanceType,
                     String proxy,
                     String serviceVersion,
                     String serviceGroup,
                     String serializationType,
                     long timeout,
                     boolean async,
                     boolean oneway,
                     int heartbeatInterval,
                     int scanNotActiveChannelInterval,
                     int retryInterval,
                     int retryTimes,
                     boolean enableResultCache,
                     int resultCacheExpire,
                     boolean enableDirectServer,
                     String directServerUrl,
                     boolean enableDelayConnection,
                     int corePoolSize,
                     int maximumPoolSize,
                     String flowType,
                     boolean enableBuffer,
                     int bufferSize,
                     String reflectType,
                     Class<?> fallbackClass) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = this.getRegistryService(registryAddress,
                registryType,loadBalanceType);
        this.proxy = proxy;
        this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.retryInterval = retryInterval;
        this.retryTimes = retryTimes;
        this.enableResultCache = enableResultCache;
        this.resultCacheExpire = resultCacheExpire;
        this.enableDirectServer = enableDirectServer;
        this.directServerUrl = directServerUrl;
        this.enableDelayConnection = enableDelayConnection;
        this.concurrentThreadPool = ConcurrentThreadPool.getInstance(corePoolSize,maximumPoolSize);
        this.flowType = flowType;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;
        this.reflectType = reflectType;
        this.fallbackClass = fallbackClass;
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
        this.start();
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class,proxy);
        proxyFactory.init(
                new ProxyConfig(
                        interfaceClass,
                        serviceVersion,
                        serviceGroup,
                        timeout,
                        registryService,
                        RpcConsumer.getInstance(),
                        serializationType,
                        async,
                        oneway,
                        enableResultCache,
                        resultCacheExpire,
                        reflectType,
                        fallbackClass));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        this.start();
        return new ObjectProxy<T>(
                interfaceClass,
                serviceVersion,
                serviceGroup,
                serializationType,
                timeout,registryService,
                RpcConsumer.getInstance(),
                async,
                oneway,
                enableResultCache,
                resultCacheExpire,
                reflectType,
                fallbackClass);
    }

    /**
     * 初始化 RpcConsumer,启动连接
     */
    public void start(){
        RpcConsumer.getInstance()
                .setHeartbeatInterval(heartbeatInterval)
                .setRetryInterval(retryInterval)
                .setDirectServerUrl(directServerUrl)
                .setEnableDirectServer(enableDirectServer)
                .setRetryTimes(retryTimes)
                .setScanNotActiveChannelInterval(scanNotActiveChannelInterval)
                .setEnableDelayConnection(enableDelayConnection)
                .setConcurrentThreadPool(concurrentThreadPool)
                .setFlowPostProcessor(flowType)
                .setEnableBuffer(enableBuffer)
                .setBufferSize(bufferSize)
                .buildNettyGroup()
                .initConnection(registryService);
    }

    public void shutdown() {
        RpcConsumer.getInstance().close();
    }
}
