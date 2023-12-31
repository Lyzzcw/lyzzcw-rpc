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
package lyzzcw.work.rpc.spring.boot.starter.provider.config;

import lombok.Data;

/**
 * @author lzy
 * @version 1.0.0
 * @description SpringBootProviderConfig
 */
@Data
public final class SpringBootProviderConfig {

    /**
     * 服务地址
     */
    private String serverAddress;
    /**
     * 服务注册地址
     */
    private String serverRegistryAddress;
    /**
     * 注册中心地址
     */
    private String registryAddress;
    /**
     * 注册类型
     */
    private String registryType;
    /**
     * 负载均衡类型
     */
    private String registryLoadBalanceType;
    /**
     * 反射类型
     */
    private String reflectType;

    /**
     * 心跳时间间隔
     */
    private int heartbeatInterval;

    /**
     * 扫描并清理不活跃连接的时间间隔
     */
    private int scanNotActiveChannelInterval;

    /**
     * 是否开启结果缓存
     */
    private boolean enableResultCache;

    /**
     * 结果缓存的时长
     */
    private int resultCacheExpire;
    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maximumPoolSize;

    /**
     * 流控类型
     */
    private String flowType;

    /**
     * 最大连接限制
     */
    private int maxConnections;

    /**
     * 拒绝策略类型
     */
    private String disuseStrategyType;

    /**
     * 是否开启数据缓冲
     */
    private boolean enableBuffer;

    /**
     * 缓冲区大小
     */
    private int bufferSize;

    /**
     * 是否开启限流
     */
    private boolean enableRateLimiter;
    /**
     * 限流类型
     */
    private String rateLimiterType;
    /**
     * 在milliSeconds毫秒内最多能够通过的请求个数
     */
    private int permits;
    /**
     * 毫秒数
     */
    private int milliSeconds;
    /**
     * 当限流失败时的处理策略
     */
    private String rateLimiterFailStrategy;
    /**
     * 是否开启熔断策略
     */
    private boolean enableFusing;

    /**
     * 熔断规则标识
     */
    private String fusingType;
    /**
     * 在fusingMilliSeconds毫秒内触发熔断操作的上限值
     */
    private double totalFailure;
    /**
     * 熔断的毫秒时长
     */
    private int fusingMilliSeconds;

    /**
     * 异常后置处理器
     */
    private String exceptionPostProcessorType;


    public SpringBootProviderConfig() {
    }

    public SpringBootProviderConfig(final String serverAddress,
                                    final String registryAddress,
                                    final String registryType,
                                    final String registryLoadBalanceType,
                                    final String reflectType,
                                    final int heartbeatInterval,
                                    int scanNotActiveChannelInterval,
                                    final boolean enableResultCache,
                                    final int resultCacheExpire,
                                    final int corePoolSize,
                                    final int maximumPoolSize,
                                    final String flowType,
                                    final int maxConnections,
                                    final String disuseStrategyType,
                                    final boolean enableBuffer,
                                    final int bufferSize,
                                    final boolean enableRateLimiter,
                                    final String rateLimiterType,
                                    final int permits,
                                    final int milliSeconds,
                                    final String rateLimiterFailStrategy,
                                    final boolean enableFusing,
                                    final String fusingType,
                                    final double totalFailure,
                                    final int fusingMilliSeconds,
                                    final String exceptionPostProcessorType) {
        this.serverAddress = serverAddress;
        this.registryAddress = registryAddress;
        this.registryType = registryType;
        this.registryLoadBalanceType = registryLoadBalanceType;
        this.reflectType = reflectType;
        if (heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.enableResultCache = enableResultCache;
        this.resultCacheExpire = resultCacheExpire;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.flowType = flowType;
        this.maxConnections = maxConnections;
        this.disuseStrategyType = disuseStrategyType;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;
        this.enableRateLimiter = enableRateLimiter;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.milliSeconds = milliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;
        this.enableFusing = enableFusing;
        this.fusingType = fusingType;
        this.totalFailure = totalFailure;
        this.fusingMilliSeconds = fusingMilliSeconds;
        this.exceptionPostProcessorType = exceptionPostProcessorType;
    }
}
