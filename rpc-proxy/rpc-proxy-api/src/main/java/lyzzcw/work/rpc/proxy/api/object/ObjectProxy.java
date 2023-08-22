package lyzzcw.work.rpc.proxy.api.object;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.cache.result.CacheResultKey;
import lyzzcw.work.rpc.cache.result.CacheResultManager;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.exception.monitor.processor.ExceptionPostProcessor;
import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeaderFactory;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.proxy.api.async.IAsyncObjectProxy;
import lyzzcw.work.rpc.proxy.api.consumer.Consumer;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;
import lyzzcw.work.rpc.reflect.api.ReflectInvoker;
import lyzzcw.work.rpc.registry.api.RegistryService;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 17:10
 * Description: 动态代理的执行类
 */
@Slf4j
public class ObjectProxy <T> implements IAsyncObjectProxy,InvocationHandler {
    /**
     * 接口的Class对象
     */
    private Class<T> clazz;
    /**
     * 服务版本号
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 超时时间，默认15s
     */
    private long timeout = 15000;
    /**
     * 服务注册与发现实例
     */
    private RegistryService registryService;
    /**
     * 服务消费者
     */
    private Consumer consumer;
    /**
     * 序列化类型
     */
    private String serializationType;
    /**
     * 是否异步调用
     */
    private boolean async;
    /**
     * 是否单向调用
     */
    private boolean oneway;
    /**
     * 是否开启结果缓存
     */
    private boolean enableResultCache;

    /**
     * 结果缓存管理器
     */
    private CacheResultManager<Object> cacheResultManager;

    /**
     * 反射调用方法
     */
    private ReflectInvoker reflectInvoker;

    /**
     * 容错Class类
     */
    private Class<?> fallbackClass;

    /**
     * 异常后置处理器
     */
    private ExceptionPostProcessor exceptionPostProcessor;


    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }
    public ObjectProxy(Class<T> clazz,
                       String serviceVersion,
                       String serviceGroup,
                       String serializationType,
                       long timeout,
                       RegistryService registryService,
                       Consumer consumer,
                       boolean async,
                       boolean oneway,
                       boolean enableResultCache,
                       int resultCacheExpire,
                       String reflectType,
                       Class<?> fallbackClass,
                       String exceptionPostProcessorType) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.registryService = registryService;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.enableResultCache = enableResultCache;
        if (resultCacheExpire <= 0){
            resultCacheExpire = RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;
        }
        this.cacheResultManager = CacheResultManager.getInstance(resultCacheExpire, enableResultCache);
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
        this.fallbackClass = fallbackClass;
        this.exceptionPostProcessor =ExtensionLoader.getExtension(ExceptionPostProcessor.class,exceptionPostProcessorType);
    }

    /**
     * 容错class为空
     */
    private boolean isFallbackClassEmpty(Class<?> fallbackClass){
        return fallbackClass == null
                || fallbackClass == RpcConstants.DEFAULT_FALLBACK_CLASS
                || RpcConstants.DEFAULT_FALLBACK_CLASS.equals(fallbackClass);
    }

    /**
     * 获取容错结果
     */
    private Object getFallbackResult(Method method, Object[] args) {
        try {
            //fallbackClass不为空，则执行容错处理
            if (this.isFallbackClassEmpty(fallbackClass)){
                return null;
            }
            return reflectInvoker.invokeMethod(fallbackClass.newInstance(), fallbackClass, method.getName(), method.getParameterTypes(), args);
        } catch (Throwable ex) {
            log.error("get fallback result error",ex);
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        //开启缓存，直接调用方法请求服务提供者
        if (enableResultCache) return invokeSendRequestMethodCache(method, args);
        return invokeSendRequestMethod(method, args);
    }

    /**
     * 获取缓存结果
     * @param method
     * @param args
     * @return
     * @throws Exception
     */
    private Object invokeSendRequestMethodCache(Method method, Object[] args) throws Exception {
        //开启缓存，则处理缓存
        CacheResultKey cacheResultKey = new CacheResultKey(method.getDeclaringClass().getName(), method.getName(), method.getParameterTypes(), args, serviceVersion, serviceGroup);
        Object obj = this.cacheResultManager.get(cacheResultKey);
        if (obj == null){
            if(log.isDebugEnabled()){
                log.debug("Cache missed...");
            }
            obj = invokeSendRequestMethod(method, args);
            if (obj != null){
                cacheResultKey.setCacheTimeStamp(System.currentTimeMillis());
                this.cacheResultManager.put(cacheResultKey, obj);
            }
        }else {
            if(log.isDebugEnabled()){
                log.debug("Cache hit:{}",obj);
            }
        }
        return obj;
    }

    /**
     * 调用远程服务器获取结果
     * @param method
     * @param args
     * @return
     * @throws Exception
     */
    private Object invokeSendRequestMethod(Method method, Object[] args) throws Exception {
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<RpcRequest>();
        requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType, RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setGroup(this.serviceGroup);
        request.setParameters(args);
        request.setAsync(async);
        request.setOneway(oneway);
        requestRpcProtocol.setBody(request);
        if(log.isDebugEnabled()){
            log.debug("request service class name:{}", method.getDeclaringClass().getName());
            log.debug("request service method name:{}", method.getName());
            if (method.getParameterTypes() != null && method.getParameterTypes().length > 0){
                for (int i = 0; i < method.getParameterTypes().length; ++i) {
                    log.debug("request service parameter type:{}", method.getParameterTypes()[i].getName());
                }
            }
            if (args != null && args.length > 0){
                for (int i = 0; i < args.length; ++i) {
                    log.debug("request service parameters:{}", args[i].toString());
                }
            }
        }

        try {
            RpcFuture rpcFuture = this.consumer.sendRequest(requestRpcProtocol,registryService);
            return rpcFuture == null ? null : timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS) : rpcFuture.get();
        }catch (Throwable ex){
            exceptionPostProcessor.postExceptionProcessor(ex);
            if(this.isFallbackClassEmpty(fallbackClass)){
                return null;
            }
            return getFallbackResult(method,args);
        }

    }

    @Override
    public RpcFuture call(String funcName, Object... args) {
        RpcProtocol<RpcRequest> request = createRequest(this.clazz.getName(), funcName, args);
        RpcFuture rpcFuture = null;
        try {
            rpcFuture = this.consumer.sendRequest(request,registryService);
        } catch (Exception e) {
            exceptionPostProcessor.postExceptionProcessor(e);
            log.error("async all throws exception:", e);
        }
        return rpcFuture;
    }

    private RpcProtocol<RpcRequest> createRequest(String className, String methodName, Object[] args) {
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<RpcRequest>();
        requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType,RpcType.REQUEST.getType()));
        RpcRequest request = new RpcRequest();
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setVersion(this.serviceVersion);
        request.setGroup(this.serviceGroup);
        Class[] parameterTypes = new Class[args.length];
        // Get the right class type
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);
        requestRpcProtocol.setBody(request);
        if(log.isDebugEnabled()){
            log.debug("request service class name:{}",className);
            log.debug("request service method name:{}",methodName);
            for (int i = 0; i < parameterTypes.length; ++i) {
                log.debug("request service parameter type:{}",parameterTypes[i].getName());
            }
            for (int i = 0; i < args.length; ++i) {
                log.debug("async all throws exception:",args[i].toString());
            }
        }
        return requestRpcProtocol;
    }

    private Class<?> getClassType(Object obj){
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName){
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }
        return classType;
    }

}
