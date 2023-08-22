package lyzzcw.work.rpc.annotation;

import lyzzcw.work.rpc.constant.RpcConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/7/20 10:20
 * Description: rpc服务消费者，配置优先级：服务消费者字段上配置的@RpcReference注解属性 > yml文件 > @RpcReference默认注解属性
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Autowired
public @interface RpcReference {
    /**
     * 版本号
     */
    String version() default RpcConstants.RPC_COMMON_DEFAULT_VERSION;
    /**
     * 服务分组，默认为空
     */
    String group() default RpcConstants.RPC_COMMON_DEFAULT_GROUP;
    /**
     * 注册中心类型，zookeeper,nacos,etcd,consul
     */
    String registryType() default RpcConstants.RPC_REFERENCE_DEFAULT_REGISTRYTYPE;
    /**
     * 注册地址
     */
    String registryAddress() default RpcConstants.RPC_REFERENCE_DEFAULT_REGISTRYADDRESS;
    /**
     * 负载均衡类型，默认基于ZK的一致性hash
     */
    String loadBalanceType() default RpcConstants.RPC_REFERENCE_DEFAULT_LOADBALANCETYPE;
    /**
     * 序列化类型，目前的类型包括：protostuff,kryo,json,jdk,hessian2,fst
     */
    String serializationType() default RpcConstants.RPC_REFERENCE_DEFAULT_SERIALIZATIONTYPE;
    /**
     * 超时时间,默认5s
     */
    long timeout() default RpcConstants.RPC_REFERENCE_DEFAULT_TIMEOUT;
    /**
     * 是否异步执行
     */
    boolean async() default false;
    /**
     * 是否单向调用
     */
    boolean oneway() default false;
    /**
     * 代理的类型:jdk,javassist,cglib
     */
    String proxy() default RpcConstants.RPC_REFERENCE_DEFAULT_PROXY;
    /**
     * 心跳间隔时间，默认30秒
     */
    int heartbeatInterval() default RpcConstants.RPC_COMMON_DEFAULT_HEARTBEATINTERVAL;

    /**
     * 扫描空闲连接间隔时间，默认60秒
     */
    int scanNotActiveChannelInterval() default RpcConstants.RPC_COMMON_DEFAULT_SCANNOTACTIVECHANNELINTERVAL;

    /**
     * 重试间隔时间
     */
    int retryInterval() default RpcConstants.RPC_REFERENCE_DEFAULT_RETRYINTERVAL;

    /**
     * 重试间隔时间
     */
    int retryTimes() default RpcConstants.RPC_REFERENCE_DEFAULT_RETRYTIMES;

    /**
     * 是否开启结果缓存
     */
    boolean enableResultCache() default false;

    /**
     * 缓存结果的时长，单位是毫秒
     */
    int resultCacheExpire() default RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;

    /**
     * 是否开启直连服务
     */
    boolean enableDirectServer() default false;

    /**
     * 直连服务的地址
     */
    String directServerUrl() default RpcConstants.RPC_COMMON_DEFAULT_DIRECT_SERVER;

    /**
     * 是否开启延迟连接
     */
    boolean enableDelayConnection() default true;

    /**
     * 默认并发线程池核心线程数
     */
    int corePoolSize() default RpcConstants.DEFAULT_CORE_POOL_SIZE;

    /**
     * 默认并发线程池最大线程数
     */
    int maximumPoolSize() default RpcConstants.DEFAULT_MAXI_NUM_POOL_SIZE;

    /**
     * 流控方式
     */
    String flowType() default RpcConstants.FLOW_POST_PROCESSOR_PRINT;

    /**
     * 是否开启缓冲区
     */
    boolean enableBuffer() default false;

    /**
     * 缓冲区大小
     */
    int bufferSize() default RpcConstants.DEFAULT_BUFFER_SIZE;

    /**
     * 容错class
     */
    Class<?> fallbackClass() default void.class;

    /**
     * 反射类型
     */
    String reflectType() default RpcConstants.DEFAULT_REFLECT_TYPE;

    /**
     * 异常后置处理器类型
     */
    String exceptionPostProcessorType() default RpcConstants.EXCEPTION_POST_PROCESSOR_PRINT;
}
