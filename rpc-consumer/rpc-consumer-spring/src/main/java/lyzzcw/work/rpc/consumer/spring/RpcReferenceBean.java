/**
 * Copyright 2020-9999 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lyzzcw.work.rpc.consumer.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lyzzcw.work.rpc.consumer.RpcClient;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author lzy
 * @version 1.0.0
 * @description RpcReferenceBean
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcReferenceBean implements FactoryBean<Object> {
    /**
     * 生成的代理对象
     */
    private Object object;
    /**
     * 接口类型
     */
    private Class<?> interfaceClass;
    /**
     * 版本号
     */
    private String version;
    /**
     * 注册中心类型：zookeeper/nacos/apoll/etcd/eureka等
     */
    private String registryType;

    /**
     * 负载均衡类型：robin
     */
    private String loadBalanceType;

    /**
     * 序列化类型：fst/kryo/protostuff/jdk/hessian2/json
     */
    private String serializationType;

    /**
     * 注册中心地址
     */
    private String registryAddress;
    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 服务分组
     */
    private String group;
    /**
     * 是否异步
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;
    /**
     * 代理方式
     */
    private String proxy;

    /**
     * 扫描空闲连接时间，默认60秒
     */
    private int scanNotActiveChannelInterval;

    /**
     * 心跳检测时间
     */
    private int heartbeatInterval;

    //重试间隔时间
    private int retryInterval = 1000;

    //重试次数
    private int retryTimes = 3;

    private RpcClient rpcClient;

    /**
     * 是否开启结果缓存
     */
    private boolean enableResultCache;

    /**
     * 缓存结果的时长，单位是毫秒
     */
    private int resultCacheExpire;

    /**
     * 是否开启直连服务
     */
    private boolean enableDirectServer;

    /**
     * 直连服务的地址
     */
    private String directServerUrl;

    /**
     * 是否开启延迟连接
     */
    private boolean enableDelayConnection;

    /**
     * 并发线程池核心线程数
     */
    private int corePoolSize;

    /**
     * 并发线程池最大线程数
     */
    private int maximumPoolSize;

    /**
     * 流控方式
     */
    private String flowType;

    /**
     * 是否开启数据缓冲
     */
    private boolean enableBuffer;

    /**
     * 缓冲区大小
     */
    private int bufferSize;

    /**
     * 反射类型
     */
    private String reflectType;

    /**
     * 容错类
     */
    private Class<?> fallbackClass;

    /**
     * 异常后置处理器类型
     */
    private String exceptionPostProcessorType;

    @Override
    public Object getObject(){
        return object;
    }

    @Override
    public Class<?> getObjectType() {
       return interfaceClass;
    }

    @SuppressWarnings("unchecked")
    public void init(){
        rpcClient = new RpcClient(
                registryAddress,
                registryType,
                loadBalanceType,
                proxy,
                version,
                group,
                serializationType,
                timeout,
                async,
                oneway,
                heartbeatInterval,
                scanNotActiveChannelInterval,
                retryInterval,
                retryTimes,
                enableResultCache,
                resultCacheExpire,
                enableDirectServer,
                directServerUrl,
                enableDelayConnection,
                corePoolSize,
                maximumPoolSize,
                flowType,
                enableBuffer,
                bufferSize,
                reflectType,
                fallbackClass,
                exceptionPostProcessorType);
        this.object = rpcClient.create(interfaceClass);
    }

}
