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
}
