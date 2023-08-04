package lyzzcw.work.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeaderFactory;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.SocketAddress;
import java.util.Map;
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
    private Map<Long,RpcProtocol<RpcResponse>> pendingResponse = new ConcurrentHashMap<>();

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

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) throws Exception {
        Assert.notNull(protocol, "consumer received none protocol");
        log.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));
        Long requestId = protocol.getHeader().getRequestId();
        this.pendingResponse.put(requestId,protocol);
    }

    /**
     * 服务消费者向服务请求者发送请求
     * @param protocol
     */
    public Object sendRequest(RpcProtocol<RpcRequest> protocol){
        log.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
        channel.writeAndFlush(protocol);
        while (true) {
            RpcProtocol<RpcResponse> responseRpcProtocol =
                    pendingResponse.remove(protocol.getHeader().getRequestId());
            if(responseRpcProtocol != null){
                return responseRpcProtocol.getBody().getResult();
            }
        }
    }

    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}