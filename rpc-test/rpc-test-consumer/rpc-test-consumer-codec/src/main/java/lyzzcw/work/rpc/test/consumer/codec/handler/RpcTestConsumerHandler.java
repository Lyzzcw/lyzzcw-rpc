package lyzzcw.work.rpc.test.consumer.codec.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeaderFactory;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;

/**
 * @author lzy
 * @version 1.0.0
 * @description RPC消费者处理器
 */
@Slf4j
public class RpcTestConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("发送数据开始...");
        //模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<RpcRequest>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk", RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setClassName("lyzzcw.work.rpc.test.api.DemoService");
        request.setGroup("lzy");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"lzy",28});
        request.setParameterTypes(new Class[]{String.class,Integer.class});
        request.setVersion("1.0.0");
        request.setAsync(false);
        request.setOneway(false);
        protocol.setBody(request);
        log.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
        ctx.writeAndFlush(protocol);
        log.info("发送数据完毕...");

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) throws Exception {
        log.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));
    }
}