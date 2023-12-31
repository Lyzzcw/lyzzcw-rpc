package lyzzcw.work.rpc.consumer.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.annotation.RpcReference;
import lyzzcw.work.rpc.common.exception.RpcException;
import lyzzcw.work.rpc.common.helper.RpcServiceHelper;
import lyzzcw.work.rpc.common.ip.IpUtils;
import lyzzcw.work.rpc.common.scanner.reference.RpcReferenceContext;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.consumer.common.handler.RpcConsumerHandler;
import lyzzcw.work.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import lyzzcw.work.rpc.consumer.common.initializer.RpcConsumerInitializer;
import lyzzcw.work.rpc.consumer.common.manager.ConsumerConnectionManager;
import lyzzcw.work.rpc.exception.monitor.processor.ExceptionPostProcessor;
import lyzzcw.work.rpc.flow.processor.FlowPostProcessor;
import lyzzcw.work.rpc.loadbalancer.context.ConnectionsContext;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.meta.ServiceMeta;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.proxy.api.consumer.Consumer;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;
import lyzzcw.work.rpc.registry.api.RegistryService;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;
import lyzzcw.work.rpc.threadpool.ConcurrentThreadPool;
import org.apache.commons.lang3.StringUtils;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 9:47
 * Description: 服务消费者
 */
@Slf4j
public class RpcConsumer implements Consumer {
    private Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    //并发处理线程池
    private ConcurrentThreadPool concurrentThreadPool;
    //本地IP
    private final String localIp;
    //心跳定时任务线程池
    private ScheduledExecutorService executorService;
    //心跳间隔时间，默认30秒
    private int heartbeatInterval = 30000;
    //扫描并移除空闲连接时间，默认60秒
    private int scanNotActiveChannelInterval = 60000;
    //重试间隔时间
    private int retryInterval = 1000;
    //重试次数
    private int retryTimes = 3;
    //当前重试次数
    private volatile int currentConnectRetryTimes = 0;

    //用于灰度发布或者方便测试
    //是否开启直连服务
    private boolean enableDirectServer = false;
    //直连服务的地址
    private String directServerUrl;
    //是否开启延迟连接(懒加载，需要时在发起连接)
    private boolean enableDelayConnection = true;
    //未开启延迟连接时，是否已经初始化连接
    private volatile boolean initConnection = false;
    //流控分析处理器
    private FlowPostProcessor flowPostProcessor;
    //是否开启数据缓冲
    private boolean enableBuffer;
    //缓冲区大小
    private int bufferSize;
    //异常后置处理器
    private ExceptionPostProcessor exceptionPostProcessor;

    private RpcConsumer(){
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        localIp = IpUtils.getLocalHostIp();
    }

    public RpcConsumer buildNettyGroup(){
        try {
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcConsumerInitializer(
                            heartbeatInterval,
                            enableBuffer,
                            bufferSize,
                            concurrentThreadPool,
                            flowPostProcessor,
                            exceptionPostProcessor));
        }catch (IllegalStateException e){
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcConsumerInitializer(
                            heartbeatInterval,
                            enableBuffer,
                            bufferSize,
                            concurrentThreadPool,
                            flowPostProcessor,
                            exceptionPostProcessor));
        }
        return this;
    }

    public void initConnection(RegistryService registryService){
        //未开启延迟连接，并且未初始化连接
        if (!enableDelayConnection && !initConnection){
            //遍历通过RpcReference注解存在缓存中的服务注册信息，通过服务名称去注册中心中拿到provider的服务地址
            //在项目初始化时建立netty连接
            Map<String, Object> cache =  RpcReferenceContext.getInstance();
            cache.forEach((key,value) -> {
                RpcReference rpcReference = (RpcReference)value;
                int h;
                int invokeHashCode = (rpcReference == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
                try {
                    ServiceMeta serviceMeta = this.getDirectServiceMetaOrWithRetry(registryService, key, invokeHashCode);
                    if (serviceMeta != null){
                        this.getRpcConsumerHandlerWithRetry(serviceMeta);
                    }
                    this.initConnection = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }
        //开启心跳
        this.startHeartbeat();
    }

    public RpcConsumer setExceptionPostProcessor(String exceptionPostProcessorType){
        this.exceptionPostProcessor = ExtensionLoader.getExtension(ExceptionPostProcessor.class,exceptionPostProcessorType);
        return this;
    }

    public RpcConsumer setEnableBuffer(boolean enableBuffer) {
        this.enableBuffer = enableBuffer;
        return this;
    }

    public RpcConsumer setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public RpcConsumer setFlowPostProcessor(String flowType){
        if (StringUtils.isEmpty(flowType)){
            flowType = RpcConstants.FLOW_POST_PROCESSOR_PRINT;
        }
        this.flowPostProcessor = ExtensionLoader.getExtension(FlowPostProcessor.class, flowType);
        return this;
    }

    public RpcConsumer setConcurrentThreadPool(ConcurrentThreadPool concurrentThreadPool) {
        this.concurrentThreadPool = concurrentThreadPool;
        return this;
    }

    public RpcConsumer setEnableDelayConnection(boolean enableDelayConnection) {
        this.enableDelayConnection = enableDelayConnection;
        return this;
    }

    public RpcConsumer setEnableDirectServer(boolean enableDirectServer) {
        this.enableDirectServer = enableDirectServer;
        return this;
    }

    public RpcConsumer setDirectServerUrl(String directServerUrl) {
        this.directServerUrl = directServerUrl;
        return this;
    }

    public RpcConsumer setHeartbeatInterval(int heartbeatInterval) {
        if (heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        return this;
    }

    public RpcConsumer setScanNotActiveChannelInterval(int scanNotActiveChannelInterval) {
        if (scanNotActiveChannelInterval > 0){
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        return this;
    }

    public RpcConsumer setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval <= 0 ? RpcConstants.DEFAULT_RETRY_INTERVAL : retryInterval;
        return this;
    }

    public RpcConsumer setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes <= 0 ? RpcConstants.DEFAULT_RETRY_TIMES : retryTimes;
        return this;
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

    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);
        //扫描并处理所有不活跃的连接
        executorService.scheduleAtFixedRate(() -> {
            log.info("=============scanNotActiveChannel============");
            ConsumerConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(()->{
            log.info("=============broadcastPingMessageFromConsumer============");
            ConsumerConnectionManager.broadcastPingMessageFromConsumer();
        }, 3, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    public void close(){
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        concurrentThreadPool.stop();
        executorService.shutdown();
    }

    /**
     * 断线重连
     * @param channel
     * @throws InterruptedException
     */
    public void reconnect(Channel channel) throws InterruptedException {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String ip = socketAddress.getAddress().getHostAddress();
        int port = socketAddress.getPort();
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceAddr(ip);
        serviceMeta.setServicePort(port);
        getRpcConsumerHandlerWithRetry(serviceMeta);
    }

    @Override
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        // TODO 暂时写死，后续引入注册中心后，从注册中心获取
        RpcRequest request = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(
                request.getClassName(),request.getVersion(),request.getGroup());
        Object[] params = request.getParameters();
        int invokeHashCode = (params == null || params.length <= 0) ? serviceKey.hashCode() : params[0].hashCode();
        ServiceMeta serviceMeta = this.getDirectServiceMetaOrWithRetry(registryService, serviceKey, invokeHashCode);
        RpcConsumerHandler handler = null;
        if (serviceMeta != null){
            handler = getRpcConsumerHandlerWithRetry(serviceMeta);
        }
        RpcFuture rpcFuture = null;
        if (handler != null){
            rpcFuture = handler.sendRequest(protocol);
        }
        return rpcFuture;
    }

    /**
     * 获取服务提供者注册信息
     * @param registryService
     * @param serviceKey
     * @param invokerHashCode
     * @return
     * @throws Exception
     */
    private ServiceMeta getDirectServiceMetaOrWithRetry(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        ServiceMeta serviceMeta = null;
        if (enableDirectServer){
            serviceMeta = this.getDirectServiceMeta();
            log.info("direct service provider metadata:{}",serviceMeta);
        }else {
            serviceMeta = this.getServiceMetaWithRetry(registryService, serviceKey, invokerHashCode);
            log.info("registry center service provider metadata:{}",serviceMeta);
        }
        return serviceMeta;
    }

    /**
     * 直连服务提供者
     * @return
     */
    private ServiceMeta getDirectServiceMeta(){
        if (StringUtils.isEmpty(directServerUrl)){
            throw new RpcException("direct server url is null ...");
        }
        if (!directServerUrl.contains(RpcConstants.IP_PORT_SPLIT)){
            throw new RpcException("direct server url not contains : ");
        }
        log.info("Service consumers connect directly to service providers ===>>> {}", directServerUrl);
        ServiceMeta serviceMeta = new ServiceMeta();
        String[] directServerUrlArray = directServerUrl.split(RpcConstants.IP_PORT_SPLIT);
        serviceMeta.setServiceAddr(directServerUrlArray[0]);
        serviceMeta.setServicePort(Integer.parseInt(directServerUrlArray[1]));
        return serviceMeta;
    }

    /**
     * 获取服务提供者元数据
     * @param registryService
     * @param serviceKey
     * @param invokerHashCode
     * @return
     * @throws Exception
     */
    private ServiceMeta getServiceMetaWithRetry(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        //首次获取服务元数据信息，如果获取到，则直接返回，否则进行重试
        if(log.isDebugEnabled()){
            log.debug("Get the service provider metadata...");
        }
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode, localIp);
        //启动重试机制
        if (serviceMeta == null){
            for (int i = 1; i <= retryTimes; i++){
                log.warn("Get service provider metadata [{}] retry for the first time...", i);
                serviceMeta = registryService.discovery(serviceKey, invokerHashCode, localIp);
                if (serviceMeta != null){
                    break;
                }
                Thread.sleep(retryInterval);
            }
        }
        if(serviceMeta == null){
            log.warn("Failed to get service provider metadata");
        }
        return serviceMeta;
    }

    /**
     * 获取RpcConsumerHandler
     */
    private RpcConsumerHandler getRpcConsumerHandlerWithRetry(ServiceMeta serviceMeta) throws InterruptedException{
        log.info("Service consumers connect service providers...");
        RpcConsumerHandler handler = null;
        try {
            handler = this.getRpcConsumerHandlerWithCache(serviceMeta);
        }catch (Exception e){
            //连接异常
            if (e instanceof ConnectException){
                //启动重试机制
                if (handler == null) {
                    if (currentConnectRetryTimes < retryTimes){
                        currentConnectRetryTimes++;
                        log.info("The service consumer connects to the service provider for the [{}] retry...", currentConnectRetryTimes);
                        handler = this.getRpcConsumerHandlerWithRetry(serviceMeta);
                        Thread.sleep(retryInterval);
                    }
                }
            }
        }
        return handler;
    }

    /**
     * 从缓存中获取RpcConsumerHandler，缓存中没有，再创建
     */
    private RpcConsumerHandler getRpcConsumerHandlerWithCache(ServiceMeta serviceMeta) throws InterruptedException{
        RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
        //缓存中无RpcClientHandler
        if (handler == null){
            handler = getRpcConsumerHandler(serviceMeta);
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        }else if (!handler.getChannel().isActive()){  //缓存中存在RpcClientHandler，但不活跃
            handler.close();
            handler = getRpcConsumerHandler(serviceMeta);
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        }
        return handler;
    }

    /**
     * 创建连接并返回RpcClientHandler
     */
    private RpcConsumerHandler getRpcConsumerHandler(ServiceMeta serviceMeta) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(serviceMeta.getServiceAddr(), serviceMeta.getServicePort()).sync();
        channelFuture.addListener((ChannelFutureListener) listener -> {
            if (channelFuture.isSuccess()) {
                log.info("connect rpc server {} on port {} success.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                //添加连接信息，在服务消费者端记录每个服务提供者实例的连接次数
                ConnectionsContext.add(serviceMeta);
                //连接成功，将当前连接重试次数设置为0
                currentConnectRetryTimes = 0;
            } else {
                log.error("connect rpc server {} on port {} failed.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }
}
