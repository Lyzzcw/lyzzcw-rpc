package lyzzcw.work.rpc.consumer.common.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lyzzcw.work.rpc.codec.RpcDecoder;
import lyzzcw.work.rpc.codec.RpcEncoder;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.consumer.common.handler.RpcConsumerHandler;
import lyzzcw.work.rpc.threadpool.ConcurrentThreadPool;

import java.util.concurrent.TimeUnit;


/**
 * @author lzy
 * @version 1.0.0
 * @description
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {
    private int heartbeatInterval;
    private ConcurrentThreadPool concurrentThreadPool;

    public RpcConsumerInitializer(int heartbeatInterval, ConcurrentThreadPool concurrentThreadPool){
        if (heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        this.concurrentThreadPool = concurrentThreadPool;
    }
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder());
        cp.addLast(RpcConstants.CODEC_DECODER, new RpcDecoder());
        cp.addLast(RpcConstants.CODEC_CLIENT_IDLE_HANDLER,
                new IdleStateHandler(heartbeatInterval, 0, 0, TimeUnit.MILLISECONDS));
        cp.addLast(RpcConstants.CODEC_HANDLER,new RpcConsumerHandler(concurrentThreadPool));
    }
}