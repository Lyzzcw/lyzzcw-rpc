package lyzzcw.work.rpc.provider.common.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.codec.RpcDecoder;
import lyzzcw.work.rpc.codec.RpcEncoder;
import lyzzcw.work.rpc.provider.common.handler.RpcProviderHandler;
import lyzzcw.work.rpc.provider.common.server.api.Server;
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
    private String host = "127.0.0.1";
    //端口号
    private int port = 27110;


    public BaseServer(String serverAddress) {
        if (!StringUtils.isEmpty(serverAddress)){
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
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
                            ch.pipeline().addLast(new RpcProviderHandler(handlerMap));
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
