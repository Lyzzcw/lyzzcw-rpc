package lyzzcw.work.rpc.provider.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/7/25 13:45
 * Description: RPC服务生产者的Handler处理类
 */
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * 存储服务提供者中被@RpcService注解标注的类的对象
     * key为：serviceName#serviceVersion#group
     * value为：@RpcService注解标注的类的对象
     */
    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
        log.info("RPC 生产者收到的数据为===>" + o.toString());
        log.info("handlerMap中存放的数据如下所示：");
        handlerMap.forEach((k,v) -> {
            System.out.println(k + "=" + v);
        });

        // 直接返回数据
        ctx.writeAndFlush(o);
    }
}
