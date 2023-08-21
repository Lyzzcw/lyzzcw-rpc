package lyzzcw.work.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.buffer.cache.BufferCacheManager;
import lyzzcw.work.rpc.buffer.object.BufferObject;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.consumer.common.cache.ConsumerChannelCache;
import lyzzcw.work.rpc.consumer.common.context.RpcContext;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcStatus;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.protocol.header.RpcHeaderFactory;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;
import lyzzcw.work.rpc.threadpool.BufferCacheThreadPool;
import lyzzcw.work.rpc.threadpool.ConcurrentThreadPool;
import org.springframework.util.Assert;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzy
 * @version 1.0.0
 * @description RPC消费者处理器
 */
@Slf4j
@Getter
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    private volatile Channel channel;
    private SocketAddress remotePeer;
    private Map<Long, RpcFuture> pendingResponse = new ConcurrentHashMap<>();
    //并发处理线程池
    private ConcurrentThreadPool concurrentThreadPool;
    //是否开启缓冲区
    private boolean enableBuffer;
    //缓冲区管理器
    private BufferCacheManager<BufferObject<RpcResponse>> bufferCacheManager;

    public RpcConsumerHandler(boolean enableBuffer,
                              int bufferSize,
                              ConcurrentThreadPool concurrentThreadPool){
        this.concurrentThreadPool = concurrentThreadPool;
        this.enableBuffer = enableBuffer;
        this.initBuffer(bufferSize);
    }
    //netty 激活连接
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
        ConsumerChannelCache.add(channel);
    }
    //netty 注册连接
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }
    //netty 断开连接
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConsumerChannelCache.remove(ctx.channel());
    }
    //netty 抛出异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx,cause);
        ConsumerChannelCache.remove(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            //发送一次心跳数据
            RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_CONSUMER_TO_PROVIDER_PING.getType());
            RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<RpcRequest>();
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
            requestRpcProtocol.setHeader(header);
            requestRpcProtocol.setBody(rpcRequest);
            ctx.writeAndFlush(requestRpcProtocol);
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) throws Exception {
        Assert.notNull(protocol, "consumer received none protocol");
        log.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));
        concurrentThreadPool.submit(() -> {
            this.handlerMessage(ctx,protocol);
        });
    }

    /**
     * 初始化缓冲区数据
     */
    private void initBuffer(int bufferSize) {
        //开启缓冲
        if (enableBuffer){
            bufferCacheManager = BufferCacheManager.getInstance(bufferSize);
            BufferCacheThreadPool.submit(() -> {
                consumerBufferCache();
            });
        }
    }

    /**
     * 消费缓冲区的数据
     */
    private void consumerBufferCache(){
        //不断消息缓冲区的数据
        while (true){
            BufferObject<RpcResponse> bufferObject = this.bufferCacheManager.take();
            if (bufferObject != null){
                this.handlerResponseMessage(bufferObject.getProtocol());
            }
        }
    }

    private void handlerMessage(ChannelHandlerContext ctx,RpcProtocol<RpcResponse> protocol) {
        RpcHeader header = protocol.getHeader();
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_PROVIDER_TO_CONSUMER_PONG.getType()){
            this.handlerHeartbeatMessageToConsumer(protocol, ctx.channel());
        }else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_PROVIDER_TO_CONSUMER_PING.getType()){
            this.handlerHeartbeatMessageFromProvider(protocol, ctx.channel());
        }else if (header.getMsgType() == (byte) RpcType.RESPONSE.getType()){ //请求消息
            this.handlerResponseMessageOrBuffer(ctx,protocol);
        }
    }

    /**
     * 包含是否开启了缓冲区的响应消息
     */
    private void handlerResponseMessageOrBuffer(ChannelHandlerContext ctx,RpcProtocol<RpcResponse> protocol){
        if (enableBuffer){
            log.info("enable buffer...");
            this.bufferCacheManager.put(new BufferObject<>(ctx, protocol));
        }else {
            this.handlerResponseMessage(protocol);
        }
    }

    private void handlerResponseMessage(RpcProtocol<RpcResponse> protocol) {
        Long requestId = protocol.getHeader().getRequestId();
        RpcFuture future = pendingResponse.remove(requestId);
        Optional.ofNullable(future).ifPresent(f->{
            future.done(protocol);
        });
    }

    /**
     * 处理心跳消息
     */
    private void handlerHeartbeatMessageToConsumer(RpcProtocol<RpcResponse> protocol, Channel channel) {
        //此处简单打印即可,实际场景可不做处理
        log.info("receive service provider heartbeat message, " +
                "the provider is: {}, the heartbeat message is: {}",
                channel.remoteAddress(), protocol.getBody().getResult());
        ConsumerChannelCache.cleanPendingPong(channel);
    }

    /**
     * 处理从服务提供者发送过来的心跳消息
     */
    private void handlerHeartbeatMessageFromProvider(RpcProtocol<RpcResponse> protocol, Channel channel) {
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.HEARTBEAT_CONSUMER_TO_PROVIDER_PONG.getType());
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<RpcRequest>();
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEARTBEAT_PONG});
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        requestRpcProtocol.setHeader(header);
        requestRpcProtocol.setBody(request);
        channel.writeAndFlush(requestRpcProtocol);
    }

    /**
     * 服务消费者向服务请求者发送请求
     * @param protocol
     */
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol) {
        log.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
        return concurrentThreadPool.submit(() -> {
            return protocol.getBody().isOneway() ? this.sendRequestOneway(protocol) : protocol.getBody().isAsync() ? sendRequestAsync(protocol) : this.sendRequestSync(protocol);
        });
    }

    /**
     * 同步调用 -> 服务消费者向服务请求者发送请求
     * @param protocol
     */
    public RpcFuture sendRequestSync(RpcProtocol<RpcRequest> protocol){
        RpcFuture future = this.getRpcFuture(protocol);
        channel.writeAndFlush(protocol);
        return future;
    }
    private RpcFuture getRpcFuture(RpcProtocol<RpcRequest> protocol){
        RpcFuture rpcFuture = new RpcFuture(protocol,concurrentThreadPool);
        this.pendingResponse.put(protocol.getHeader().getRequestId(), rpcFuture);
        return rpcFuture;
    }

    /**
     * 异步调用 -> 服务消费者向服务请求者发送请求
     * @param protocol
     */
    public RpcFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol){
        RpcFuture future = this.getRpcFuture(protocol);
        //如果是异步调用，则将RpcFuture放入RpcContext
        RpcContext.getContext().setRpcFuture(future);
        channel.writeAndFlush(protocol);
        return null;
    }

    /**
     * 单向调用（不需要返回结果） -> 服务消费者向服务请求者发送请求
     * @param protocol
     */
    public RpcFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol){
        channel.writeAndFlush(protocol);
        return null;
    }

    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

}