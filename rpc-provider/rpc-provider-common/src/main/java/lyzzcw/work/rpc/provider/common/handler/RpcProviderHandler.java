package lyzzcw.work.rpc.provider.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.common.helper.RpcServiceHelper;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcStatus;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.protocol.response.RpcResponse;
import lyzzcw.work.rpc.reflect.api.ReflectInvoker;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;
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
    private final ConcurrentThreadPool concurrentThreadPool =
            ConcurrentThreadPool.getInstance(2, 4);

    /**
     * 调用采用哪种类型调用真实方法
     */
    private ReflectInvoker reflectInvoker;

    public RpcProviderHandler(String reflectType, Map<String, Object> handlerMap) {
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        log.info("Rpc provider received:{}", JSONObject.toJSONString(protocol));
        log.info("handlerMap中存放的数据如下所示：");
        handlerMap.forEach((k, v) -> {
            log.info(k + "=" + v);
        });
        concurrentThreadPool.submit(() -> {
            RpcProtocol<RpcResponse> responseRpcProtocol = handlerMessage(protocol, ctx.channel());
            // 直接返回数据
            ctx.writeAndFlush(responseRpcProtocol).addListener(
                    (ChannelFutureListener) channelFuture -> {
                        if (log.isDebugEnabled()) {
                            log.debug("send response for request:{}", protocol.getHeader().getRequestId());
                        }
                    }
            );
        });
    }

    /**
     * 处理消息
     */
    private RpcProtocol<RpcResponse> handlerMessage(RpcProtocol<RpcRequest> protocol, Channel channel){
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        RpcHeader header = protocol.getHeader();
        //接收到服务消费者发送的心跳消息
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT.getType()){
            responseRpcProtocol = handlerHeartbeatMessage(protocol, header);
        }else if (header.getMsgType() == (byte) RpcType.REQUEST.getType()){ //请求消息
            responseRpcProtocol = handlerRequestMessage(protocol, header);
        }
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handlerRequestMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        RpcRequest request = protocol.getBody();
        if (log.isDebugEnabled()) {
            log.debug("receive request: {}", header.getRequestId());
        }
        //将header中的消息类型设置为响应类型的消息
        header.setMsgType((byte) RpcType.RESPONSE.getType());
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
    private RpcProtocol<RpcResponse> handlerHeartbeatMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        header.setMsgType((byte) RpcType.HEARTBEAT.getType());
        RpcRequest request = protocol.getBody();
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<RpcResponse>();
        RpcResponse response = new RpcResponse();
        response.setResult(RpcConstants.HEARTBEAT_PONG);
        response.setAsync(request.isAsync());
        response.setOneway(request.isOneway());
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        return responseRpcProtocol;
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
