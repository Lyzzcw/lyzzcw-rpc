package lyzzcw.work.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.consumer.common.context.RpcContext;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;
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

    //netty 激活连接
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
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
    }
    //netty 抛出异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx,cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) throws Exception {
        Assert.notNull(protocol, "consumer received none protocol");
        log.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));
        Long requestId = protocol.getHeader().getRequestId();
        RpcFuture future = pendingResponse.get(requestId);
        Optional.ofNullable(future).ifPresent(f->{
            future.done(protocol);
        });
    }

    /**
     * 服务消费者向服务请求者发送请求
     * @param protocol
     */
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol) {
        log.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
        if(protocol.getBody().isOneway()){
            return sendRequestOneway(protocol);
        }
        if(protocol.getBody().isAsync()){
            return sendRequestAsync(protocol);
        }
        return sendRequestSync(protocol);
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
        RpcFuture rpcFuture = new RpcFuture(protocol);
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