package lyzzcw.work.rpc.provider.common.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.codec.RpcDecoder;
import lyzzcw.work.rpc.codec.RpcEncoder;
import lyzzcw.work.rpc.provider.common.handler.RpcProviderHandler;
import lyzzcw.work.rpc.provider.common.server.api.Server;
import lyzzcw.work.rpc.registry.api.RegistryService;
import lyzzcw.work.rpc.registry.api.config.RegistryConfig;
import lyzzcw.work.rpc.registry.zookeeper.ZookeeperRegistryService;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

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
    //reflect type
    private String reflectType;
    //服务注册与发现的实例
    protected RegistryService registryService;

    public BaseServer(String serverAddress,String registryAddress,
                      String registryType,String registryLoadBalanceType,
                      String reflectType) {
        if (!StringUtils.isEmpty(serverAddress)){
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        this.reflectType = reflectType;
        this.registryService = this.getRegistryService(registryAddress,
                registryType,registryLoadBalanceType);
    }

    /**
     * 创建服务注册与发现的实例
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
            registryService = ExtensionLoader.getExtension(RegistryService.class,registryType);
            registryService.init(new RegistryConfig(registryAddress,
                    registryType,registryLoadBalanceType));
        } catch (Exception e) {
            log.error("registry service init error",e);
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

                            ch.pipeline().addLast(new RpcDecoder());
                            ch.pipeline().addLast(new RpcEncoder());
                            ch.pipeline().addLast(new RpcProviderHandler
                                    (reflectType, handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)

            log.info("Server started on {}:{}", host, port);

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();

        }catch (Exception e) {
            log.error("RPC Server start error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
