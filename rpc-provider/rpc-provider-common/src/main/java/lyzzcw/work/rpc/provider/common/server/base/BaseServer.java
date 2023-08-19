package lyzzcw.work.rpc.provider.common.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.cache.result.CacheResultKey;
import lyzzcw.work.rpc.codec.RpcDecoder;
import lyzzcw.work.rpc.codec.RpcEncoder;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;
import lyzzcw.work.rpc.provider.common.handler.RpcProviderHandler;
import lyzzcw.work.rpc.provider.common.manager.ProviderConnectionManager;
import lyzzcw.work.rpc.provider.common.server.api.Server;
import lyzzcw.work.rpc.registry.api.RegistryService;
import lyzzcw.work.rpc.registry.api.config.RegistryConfig;
import lyzzcw.work.rpc.registry.zookeeper.ZookeeperRegistryService;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/7/25 15:33
 * Description: 基础服务
 */
@Slf4j
public class BaseServer implements Server {

    protected Map<String, Object> handlerMap = new HashMap<String, Object>();

    //主机域名或者IP地址
    protected String host = "127.0.0.1";
    //端口号
    protected int port = 27110;
    //注册到注册中心的地址
    protected String serverRegistryHost;
    protected int serverRegistryPort;
    //reflect type
    private String reflectType;
    //服务注册与发现的实例
    protected RegistryService registryService;
    //心跳定时任务线程池
    private ScheduledExecutorService executorService;
    //心跳间隔时间，默认30秒
    private int heartbeatInterval = 30000;
    //扫描并移除空闲连接时间，默认60秒
    private int scanNotActiveChannelInterval = 60000;
    //结果缓存过期时长，默认5秒
    private int resultCacheExpire = 5000;
    //是否开启结果缓存
    private boolean enableResultCache;
    //核心线程数
    private int corePoolSize;
    //最大线程数
    private int maximumPoolSize;

    public BaseServer(String serverAddress,
                      String serverRegistryAddress,
                      String registryAddress,
                      String registryType,
                      String registryLoadBalanceType,
                      String reflectType,
                      int heartbeatInterval,
                      int scanNotActiveChannelInterval,
                      boolean enableResultCache,
                      int resultCacheExpire,
                      int corePoolSize,
                      int maximumPoolSize) {
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        if (!StringUtils.isEmpty(serverRegistryAddress)){
            String[] serverRegistryAddressArray = serverRegistryAddress.split(":");
            this.serverRegistryHost = serverRegistryAddressArray[0];
            this.serverRegistryPort = Integer.parseInt(serverRegistryAddressArray[1]);
        }else{
            this.serverRegistryHost = this.host;
            this.serverRegistryPort = this.port;
        }
        this.reflectType = reflectType;
        this.registryService = this.getRegistryService(registryAddress,
                registryType, registryLoadBalanceType);
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
        if (scanNotActiveChannelInterval > 0) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        if(resultCacheExpire > 0){
            this.resultCacheExpire = resultCacheExpire;
        }
        this.enableResultCache = enableResultCache;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * 启动定时任务，定时向服务消费者发送心跳，定时扫描并移除不活跃连接
     */
    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);
        //扫描并处理所有不活跃的连接
        executorService.scheduleAtFixedRate(() -> {
            log.info("=============scanNotActiveChannel============");
            ProviderConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);
        executorService.scheduleAtFixedRate(() -> {
            log.info("=============broadcastPingMessageFromConsumer============");
            ProviderConnectionManager.broadcastPingMessageFromProvider();
        }, 3, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建服务注册与发现的实例
     *
     * @param registryAddress
     * @param registryType
     * @return
     */
    private RegistryService getRegistryService(String registryAddress,
                                               String registryType,
                                               String registryLoadBalanceType) {
        //TODO 后续拓展支持SPI
        RegistryService registryService = null;
        try {
            registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
            registryService.init(new RegistryConfig(registryAddress,
                    registryType, registryLoadBalanceType));
        } catch (Exception e) {
            log.error("registry service init error", e);
        }
        return registryService;
    }

    @Override
    public void startNettyServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline().addLast(RpcConstants.CODEC_DECODER, new RpcDecoder());
                            ch.pipeline().addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder());
                            /**
                             * Netty中的IdleStateHandler对象本质上是一个Handler处理器，配置在Netty的责任链里面，当发送请求或者收到响应时，都会经过该对象处理。
                             * 在双方通讯开始后该对象会创建一些空闲检测定时器，用于检测读事件（收到请求会触发读事件）和写事件（连接、发送请求会触发写事件）。
                             * 当在指定的空闲时间内没有收到读事件或写事件，便会触发超时事件，然后IdleStateHandler将超时事件交给责任链里面的下一个Handler。
                             *
                             * 如果是在服务提供者端，检测到超时事件，可以直接关闭超时的连接。
                             * 如果是在服务消费者端，检测到超时事件，可以构造一个心跳请求对象，向服务提供者发起心跳数据。
                             *
                             * readerIdleTime：读空闲超时检测定时任务会在每readerIdleTime时间内启动一次，检测在readerIdleTime内是否发生过读事件，如果没有发生过，则触发读超时事件READER_IDLE_STATE_EVENT，并将超时事件交给NettyClientHandler处理。如果为0，则不创建定时任务。
                             * writerIdleTime：与readerIdleTime作用类似，只不过该参数定义的是写事件。
                             * allIdleTime：同时检测读事件和写事件，如果在allIdleTime时间内即没有发生过读事件，也没有发生过写事件，则触发超时事件ALL_IDLE_STATE_EVENT。
                             * unit：表示前面三个参数的单位，就上面代码来说，表示的是毫秒。
                             */
                            ch.pipeline().addLast(RpcConstants.CODEC_SERVER_IDLE_HANDLER,
                                    new IdleStateHandler(0, 0, heartbeatInterval, TimeUnit.MILLISECONDS));
                            ch.pipeline().addLast(RpcConstants.CODEC_HANDLER, new RpcProviderHandler
                                    (reflectType,
                                            enableResultCache,
                                            resultCacheExpire,
                                            corePoolSize,
                                            maximumPoolSize,
                                            handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)

            log.info("Server started on {}:{}", host, port);
            //开启心跳定时任务
            this.startHeartbeat();
            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error("RPC Server start error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
