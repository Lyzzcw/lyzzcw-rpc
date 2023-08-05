package lyzzcw.work.rpc.consumer;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.consumer.common.RpcConsumer;
import lyzzcw.work.rpc.proxy.api.ProxyFactory;
import lyzzcw.work.rpc.proxy.api.async.IAsyncObjectProxy;
import lyzzcw.work.rpc.proxy.api.config.ProxyConfig;
import lyzzcw.work.rpc.proxy.api.object.ObjectProxy;
import lyzzcw.work.rpc.proxy.jdk.JdkProxyFactory;

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
     * 是否异步调用
     */
    private boolean async;
    /**
     * 是否单向调用
     */
    private boolean oneway;
    public RpcClient(String serviceVersion, String serviceGroup, String serializationType, long timeout, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
    }
    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = new JdkProxyFactory<T>();
        proxyFactory.init(new ProxyConfig(interfaceClass,serviceVersion,
                serviceGroup, timeout, RpcConsumer.getInstance(),
                serializationType, async, oneway));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass, serviceVersion, serviceGroup, serializationType, timeout, RpcConsumer.getInstance(), async, oneway);
    }
    public void shutdown() {
        RpcConsumer.getInstance().close();
    }
}
