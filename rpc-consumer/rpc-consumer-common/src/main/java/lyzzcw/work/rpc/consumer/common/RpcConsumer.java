package lyzzcw.work.rpc.consumer.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.common.helper.RpcServiceHelper;
import lyzzcw.work.rpc.common.ip.IpUtils;
import lyzzcw.work.rpc.consumer.common.handler.RpcConsumerHandler;
import lyzzcw.work.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import lyzzcw.work.rpc.consumer.common.initializer.RpcConsumerInitializer;
import lyzzcw.work.rpc.loadbalancer.context.ConnectionsContext;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.meta.ServiceMeta;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.proxy.api.consumer.Consumer;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;
import lyzzcw.work.rpc.registry.api.RegistryService;
import lyzzcw.work.rpc.threadpool.ConcurrentThreadPool;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 9:47
 * Description: 服务消费者
 */
@Slf4j
public class RpcConsumer implements Consumer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    //并发处理线程池
    private ConcurrentThreadPool concurrentThreadPool = ConcurrentThreadPool.getInstance(2,4);
    //本地IP
    private final String localIp;

    private RpcConsumer(){
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer(concurrentThreadPool));
        localIp = IpUtils.getLocalHostIp();
    }

    //设置单例
    private static volatile RpcConsumer instance;
    public static RpcConsumer getInstance(){
        if(instance == null){
            synchronized(RpcConsumer.class){
                if(instance == null){
                    instance = new RpcConsumer();
                }
            }
        }
        return instance;
    }

    public void close(){
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        concurrentThreadPool.stop();
    }

    @Override
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        // TODO 暂时写死，后续引入注册中心后，从注册中心获取
        RpcRequest request = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(
                request.getClassName(),request.getVersion(),request.getGroup());
        Object[] params = request.getParameters();
        int invokeHashCode = (params == null || params.length <= 0) ? serviceKey.hashCode() : params[0].hashCode();
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokeHashCode,this.localIp);
        if(serviceMeta != null){
            RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
            //缓存中无RpcClientHandler
            if(handler == null){
                handler = getRpcConsumerHandler(serviceMeta);
                RpcConsumerHandlerHelper.put(serviceMeta,handler);
            }else if(!handler.getChannel().isActive()){
                //缓存中存在RpcClientHandler ,但不活跃
                handler.close();
                handler = getRpcConsumerHandler(serviceMeta);
                RpcConsumerHandlerHelper.put(serviceMeta,handler);
            }
            return handler.sendRequest(protocol);
        }
        return null;
    }

    /**
     * 创建连接并返回RpcClientHandler
     */
    private RpcConsumerHandler getRpcConsumerHandler(ServiceMeta serviceMeta) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(serviceMeta.getServiceAddr()
                ,serviceMeta.getServicePort()).sync();
        channelFuture.addListener((ChannelFutureListener) listener -> {
            if(listener.isSuccess()){
                log.info("Successfully connected rpc server {} on port {}", serviceMeta);
                //添加连接信息，在服务消费端记录每个服务提供者实例的连接次数
                ConnectionsContext.add(serviceMeta);
            }else{
                log.error("Failure connecting rpc server {} on port {}", serviceMeta);
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }
}
