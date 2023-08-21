package lyzzcw.work.rpc.provider.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.buffer.cache.BufferCacheManager;
import lyzzcw.work.rpc.buffer.object.BufferObject;
import lyzzcw.work.rpc.cache.result.CacheResultKey;
import lyzzcw.work.rpc.cache.result.CacheResultManager;
import lyzzcw.work.rpc.common.helper.RpcServiceHelper;
import lyzzcw.work.rpc.connection.manager.ConnectionManager;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcStatus;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;
import lyzzcw.work.rpc.provider.common.cache.ProviderChannelCache;
import lyzzcw.work.rpc.reflect.api.ReflectInvoker;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;
import lyzzcw.work.rpc.threadpool.BufferCacheThreadPool;
import lyzzcw.work.rpc.threadpool.ConcurrentThreadPool;

import java.util.Map;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/7/25 13:45
 * Description: RPC服务生产者的Handler处理类
 */
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    /**
     * 存储服务提供者中被@RpcService注解标注的类的对象
     * key为：serviceName#serviceVersion#group
     * value为：@RpcService注解标注的类的对象
     */
    private final Map<String, Object> handlerMap;
    /**
     * 线程池
     */
    private ConcurrentThreadPool concurrentThreadPool;

    /**
     * 调用采用哪种类型调用真实方法
     */
    private ReflectInvoker reflectInvoker;
    /**
     * 是否启用结果缓存
     */
    private final boolean enableResultCache;

    /**
     * 结果缓存管理器
     */
    private final CacheResultManager<RpcProtocol<RpcResponse>> cacheResultManager;

    /**
     * 连接管理器
     */
    private ConnectionManager connectionManager;

    /**
     * 是否开启缓冲区
     */
    private boolean enableBuffer;

    /**
     * 缓冲区管理器
     */
    private BufferCacheManager<BufferObject<RpcRequest>> bufferCacheManager;

    public RpcProviderHandler(String reflectType,
                              boolean enableResultCache,
                              int resultCacheExpire,
                              int corePoolSize,
                              int maximumPoolSize,
                              Map<String, Object> handlerMap,
                              int maxConnections,
                              String disuseStrategyType,
                              boolean enableBuffer,
                              int bufferSize) {
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
        this.handlerMap = handlerMap;
        this.enableResultCache = enableResultCache;
        if (resultCacheExpire <= 0){
            resultCacheExpire = RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;
        }
        this.cacheResultManager = CacheResultManager.getInstance(resultCacheExpire, enableResultCache);
        this.concurrentThreadPool = ConcurrentThreadPool.getInstance(corePoolSize,maximumPoolSize);
        this.connectionManager = ConnectionManager.getInstance(maxConnections, disuseStrategyType);
        this.enableBuffer = enableBuffer;
        this.initBuffer(bufferSize);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ProviderChannelCache.add(ctx.channel());
        connectionManager.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    //netty 抛出异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx,cause);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //如果是IdleStateEvent事件
        if (evt instanceof IdleStateEvent){
            Channel channel = ctx.channel();
            try{
                log.info("IdleStateEvent triggered, close channel " + channel);
                connectionManager.remove(ctx.channel());
                channel.close();
            }finally {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        log.info("handlerMap中存放的数据如下所示：");
        handlerMap.forEach((k, v) -> {
            log.info(k + "=" + v);
        });
        concurrentThreadPool.submit(() -> {
            connectionManager.update(ctx.channel());
            if (enableBuffer){  //开启队列缓冲
                log.info("Rpc buffer provider received:{}", JSONObject.toJSONString(protocol));
                this.bufferRequest(ctx, protocol);
            }else{  //未开启队列缓冲
                log.info("Rpc provider received:{}", JSONObject.toJSONString(protocol));
                this.submitRequest(ctx, protocol);
            }
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
            BufferObject<RpcRequest> bufferObject = this.bufferCacheManager.take();
            if (bufferObject != null){
                ChannelHandlerContext ctx = bufferObject.getCtx();
                RpcProtocol<RpcRequest> protocol = bufferObject.getProtocol();
                RpcHeader header = protocol.getHeader();
                RpcProtocol<RpcResponse> responseRpcProtocol = handlerRequestMessageWithCache(protocol, header);
                this.writeAndFlush(header.getRequestId(), ctx, responseRpcProtocol);
            }
        }
    }

    /**
     * 向服务消费者写回数据
     */
    private void writeAndFlush(long requestId, ChannelHandlerContext ctx,  RpcProtocol<RpcResponse> responseRpcProtocol){
        ctx.writeAndFlush(responseRpcProtocol).addListener(
                (ChannelFutureListener) channelFuture -> {
                    if (log.isDebugEnabled()) {
                        log.debug("send response for request:{}", requestId);
                    }
                }
        );
    }

    /**
     * 缓冲数据
     */
    private void bufferRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        RpcHeader header = protocol.getHeader();
        //接收到服务消费者发送的心跳消息
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_CONSUMER_TO_PROVIDER_PONG.getType()){
            //接收到服务消费者发送的心跳响应
            handlerHeartbeatMessageFromConsumer(protocol, ctx.channel());
        }else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_CONSUMER_TO_PROVIDER_PING.getType()){
            //接收到服务消费者响应的心跳消息
            handlerHeartbeatMessageToProvider(protocol, ctx.channel());
        }else if (header.getMsgType() == (byte) RpcType.REQUEST.getType()){ //请求消息
            this.bufferCacheManager.put(new BufferObject<>(ctx, protocol));
        }
    }

    /**
     * 提交请求
     */
    private void submitRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        RpcProtocol<RpcResponse> responseRpcProtocol = handlerMessage(protocol, ctx.channel());
        writeAndFlush(protocol.getHeader().getRequestId(), ctx, responseRpcProtocol);
    }

    /**
     * 处理消息
     */
    private RpcProtocol<RpcResponse> handlerMessage(RpcProtocol<RpcRequest> protocol, Channel channel){
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        RpcHeader header = protocol.getHeader();
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_CONSUMER_TO_PROVIDER_PONG.getType()){
            //接收到服务消费者发送的心跳响应
            handlerHeartbeatMessageFromConsumer(protocol, channel);
        }else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_CONSUMER_TO_PROVIDER_PING.getType()){
            //接收到服务消费者响应的心跳消息
            handlerHeartbeatMessageToProvider(protocol, channel);
        }else if (header.getMsgType() == (byte) RpcType.REQUEST.getType()){
            //请求消息
            responseRpcProtocol = handlerRequestMessageWithCache(protocol, header);
        }
        return responseRpcProtocol;
    }


    /**
     * 结合缓存处理结果
     */
    private RpcProtocol<RpcResponse> handlerRequestMessageWithCache(RpcProtocol<RpcRequest> protocol, RpcHeader header){
        //将header中的消息类型设置为响应类型的消息
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        if (enableResultCache){
            return handlerRequestMessageCache(protocol, header);
        }
        return handlerRequestMessage(protocol, header);
    }

    /**
     * 处理缓存
     */
    private RpcProtocol<RpcResponse> handlerRequestMessageCache(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        RpcRequest request = protocol.getBody();
        CacheResultKey cacheKey = new CacheResultKey(request.getClassName(), request.getMethodName(), request.getParameterTypes(), request.getParameters(), request.getVersion(), request.getGroup());
        RpcProtocol<RpcResponse> responseRpcProtocol = cacheResultManager.get(cacheKey);
        if (responseRpcProtocol == null){
            if(log.isDebugEnabled()){
                log.debug("Cache misses...");
            }
            responseRpcProtocol = handlerRequestMessage(protocol, header);
            //设置保存的时间
            cacheKey.setCacheTimeStamp(System.currentTimeMillis());
            cacheResultManager.put(cacheKey, responseRpcProtocol);
        }else {
            if(log.isDebugEnabled()){
                log.debug("Cache hits:{}",responseRpcProtocol);
            }
        }
        RpcHeader responseHeader = responseRpcProtocol.getHeader();
        responseHeader.setRequestId(header.getRequestId());
        responseRpcProtocol.setHeader(responseHeader);
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerRequestMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        RpcRequest request = protocol.getBody();
        if (log.isDebugEnabled()) {
            log.debug("receive request: {}", header.getRequestId());
        }
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<RpcResponse>();
        RpcResponse response = new RpcResponse();
        try {
            Object result = handle(request);
            response.setResult(result);
            response.setAsync(request.isAsync());
            response.setOneway(request.isOneway());
            header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        } catch (Throwable t) {
            response.setError(t.getMessage());
            header.setStatus((byte) RpcStatus.FAIL.getCode());
            log.error("rpc server handle request error", t);
        }
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        return responseRpcProtocol;
    }

    /**
     * 处理心跳
     */
    private void handlerHeartbeatMessageFromConsumer(RpcProtocol<RpcRequest> protocol,Channel channel) {
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.HEARTBEAT_PROVIDER_TO_CONSUMER_PONG.getType());
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<RpcResponse>();
        RpcResponse response = new RpcResponse();
        response.setResult(RpcConstants.HEARTBEAT_PONG);
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        channel.writeAndFlush(responseRpcProtocol);
    }


    private void handlerHeartbeatMessageToProvider(RpcProtocol<RpcRequest> protocol, Channel channel) {
        log.info("receive service consumer heartbeat message, " +
                        "the consumer is: {}, the heartbeat message is: {}",
                channel.remoteAddress(), protocol.getBody().getParameters()[0]);
        ProviderChannelCache.cleanPendingPong(channel);
    }

    private Object handle(RpcRequest request) throws Throwable {
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        if (log.isDebugEnabled()) {
            log.debug("request service class name:{}", serviceClass.getName());
            log.debug("request service method name:{}", methodName);
            if (parameterTypes != null && parameterTypes.length > 0) {
                for (int i = 0; i < parameterTypes.length; ++i) {
                    log.debug("request service parameter type:{}", parameterTypes[i].getName());
                }
            }
            if (parameters != null && parameters.length > 0) {
                for (int i = 0; i < parameters.length; ++i) {
                    log.debug("request service parameters:{}", parameters[i].toString());
                }
            }
        }
        return this.reflectInvoker.invokeMethod(serviceBean,
                serviceClass, methodName, parameterTypes, parameters);
    }


}
