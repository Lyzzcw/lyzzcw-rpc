package lyzzcw.work.rpc.provider.common.handler;

import com.alibaba.fastjson.JSONObject;
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
import lyzzcw.work.rpc.threadpool.ConcurrentThreadPool;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
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
    private final String reflectType;

    public RpcProviderHandler(String reflectType,Map<String,Object> handlerMap){
        this.reflectType = reflectType;
        this.handlerMap = handlerMap;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        log.info("Rpc provider received:{}", JSONObject.toJSONString(protocol));
        log.info("handlerMap中存放的数据如下所示：");
        handlerMap.forEach((k,v) -> {
            log.info(k + "=" + v);
        });
        concurrentThreadPool.submit(() -> {
            RpcHeader header = protocol.getHeader();
            RpcRequest request = protocol.getBody();
            if(log.isDebugEnabled()){
                log.debug("receive request: {}",header.getRequestId());
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
            }catch (Throwable t) {
                response.setError(t.getMessage());
                header.setStatus((byte) RpcStatus.FAIL.getCode());
                log.error("rpc server handle request error",t);
            }
            responseRpcProtocol.setHeader(header);
            responseRpcProtocol.setBody(response);
            // 直接返回数据
            ctx.writeAndFlush(responseRpcProtocol).addListener(
                    (ChannelFutureListener) channelFuture -> {
                        if(log.isDebugEnabled()){
                            log.debug("send response for request:{}",header.getRequestId());
                        }
                    }
            );
        });
    }

    private Object handle(RpcRequest request) throws Throwable{
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        if(log.isDebugEnabled()){
            log.debug("request service class name:{}",serviceClass.getName());
            log.debug("request service method name:{}",methodName);
            if (parameterTypes != null && parameterTypes.length > 0){
                for (int i = 0; i < parameterTypes.length; ++i) {
                    log.debug("request service parameter type:{}",parameterTypes[i].getName());
                }
            }
            if (parameters != null && parameters.length > 0){
                for (int i = 0; i < parameters.length; ++i) {
                    log.debug("request service parameters:{}",parameters[i].toString());
                }
            }
        }
        switch (this.reflectType){
            case RpcConstants.REFLECT_TYPE_JDK:
                return invokeJdkMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
            case RpcConstants.REFLECT_TYPE_CGLIB:
                return invokeCglibMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
            default:
                throw new IllegalArgumentException("not support reflect type");
        }
    }

    /**
     * jdk reflect type
     * @param serviceBean
     * @param serviceClass
     * @param methodName
     * @param parameterTypes
     * @param parameters
     * @return
     * @throws Throwable
     */
    private Object invokeJdkMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        if(log.isDebugEnabled()){
            log.debug("use jdk reflect type invoke method...");
        }
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    /**
     * cglib reflect type
     * @param serviceBean
     * @param serviceClass
     * @param methodName
     * @param parameterTypes
     * @param parameters
     * @return
     * @throws Throwable
     */
    private Object invokeCglibMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        if(log.isDebugEnabled()){
            log.debug("use cglib reflect type invoke method...");
        }
        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod fastMethod = fastClass.getMethod(methodName,parameterTypes);
        return fastMethod.invoke(serviceBean, parameters);
    }
}
