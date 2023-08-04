package lyzzcw.work.rpc.consumer.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.consumer.common.handler.RpcConsumerHandler;
import lyzzcw.work.rpc.consumer.common.initializer.RpcConsumerInitializer;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.request.RpcRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 9:47
 * Description: 服务消费者
 */
@Slf4j
public class RpcConsumer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    private RpcConsumer(){
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer());
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
        eventLoopGroup.shutdownGracefully();
    }

    public void sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception {
        // TODO 暂时写死，后续引入注册中心后，从注册中心获取
        String serverAddress = "127.0.0.1";
        int port = 27880;
        String key = serverAddress.concat("_").concat(String.valueOf(port));
        RpcConsumerHandler handler = handlerMap.get(key);
        //缓存中无RpcClientHandler
        if(handler == null){
            handler = getRpcConsumerHandler(serverAddress, port);
        }else if(!handler.getChannel().isActive()){
            //缓存中存在RpcClientHandler ,但不活跃
            handler.close();
            handler = getRpcConsumerHandler(serverAddress,port);
            handlerMap.put(key, handler);
        }
        handler.sendRequest(protocol);
    }

    /**
     * 创建连接并返回RpcClientHandler
     */
    private RpcConsumerHandler getRpcConsumerHandler(String serverAddress,int port){
        ChannelFuture channelFuture = bootstrap.connect(serverAddress,port);
        channelFuture.addListener((ChannelFutureListener) listener -> {
            if(listener.isSuccess()){
                log.info("Successfully connected rpc server {} on port {}", serverAddress,port);
            }else{
                log.info("Failure connecting rpc server {} on port {}", serverAddress,port);
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }
}
