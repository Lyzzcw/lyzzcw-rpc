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
package lyzzcw.work.rpc.demo.spring.annotation.provider.config;

import lyzzcw.work.rpc.provider.spring.RpcSpringServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author lzy
 * @version 1.0.0
 * @description 基于注解的配置类
 */
@Configuration
@ComponentScan(value = {"lyzzcw.work.rpc.demo"})
@PropertySource(value = {"classpath:rpc.properties"})
public class SpringAnnotationProviderConfig {

    @Value("${registry.address}")
    private String registryAddress;

    @Value("${registry.type}")
    private String registryType;

    @Value("${registry.loadbalance.type}")
    private String registryLoadbalancerType;

    @Value("${server.address}")
    private String serverAddress;

    @Value("${server.registry.address}")
    private String serverRegistryAddress;

    @Value("${reflect.type}")
    private String reflectType;

    @Value("${server.heartbeatInterval}")
    private Integer heartbeatInterval;

    @Value("${server.scanNotActiveChannelInterval}")
    private Integer scanNotActiveChannelInterval;

    @Value("${server.enableResultCache}")
    private Boolean enableResultCache;

    @Value("${server.resultCacheExpire}")
    private Integer resultCacheExpire;

    @Value("${server.corePoolSize}")
    private int corePoolSize;

    @Value("${server.maximumPoolSize}")
    private int maximumPoolSize;

    @Value("${server.flowType}")
    private String flowType;

    @Value("${server.maxConnections}")
    private int maxConnections;

    @Value("${server.disuseStrategyType}")
    private String disuseStrategyType;

    @Value("${server.enableBuffer}")
    private boolean enableBuffer;

    @Value("${server.bufferSize}")
    private int bufferSize;

    @Value("${server.enableRateLimiter}")
    private boolean enableRateLimiter;

    @Value("${server.rateLimiterType}")
    private String rateLimiterType;

    @Value("${server.permits}")
    private int permits;

    @Value("${server.milliSeconds}")
    private int milliSeconds;

    @Value("${server.rateLimiterFailStrategy}")
    private String rateLimiterFailStrategy;

    @Bean
    public RpcSpringServer rpcSpringServer(){
        return new RpcSpringServer(
                serverAddress,
                serverRegistryAddress,
                registryAddress,
                registryType,
                registryLoadbalancerType,
                reflectType,
                heartbeatInterval,
                scanNotActiveChannelInterval,
                enableResultCache,
                resultCacheExpire,
                corePoolSize,
                maximumPoolSize,
                flowType,
                maxConnections,
                disuseStrategyType,
                enableBuffer,
                bufferSize,
                enableRateLimiter,
                rateLimiterType,
                permits,
                milliSeconds,
                rateLimiterFailStrategy);
    }
}
