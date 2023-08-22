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
package lyzzcw.work.rpc.provider.spring;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.annotation.RpcService;
import lyzzcw.work.rpc.common.helper.RpcServiceHelper;
import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.protocol.meta.ServiceMeta;
import lyzzcw.work.rpc.provider.common.server.base.BaseServer;
import lyzzcw.work.rpc.threadpool.AsyncStartProviderThreadPool;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author lzy
 * @version 1.0.0
 * @description 基于Spring启动RPC服务
 */
@Slf4j
public class RpcSpringServer extends BaseServer implements ApplicationContextAware, InitializingBean {

    public RpcSpringServer(String serverAddress,
                           String serverRegistryAddress,
                           String registryAddress,
                           String registryType,
                           String registryLoadBalanceType,
                           String reflectType,
                           int heartbeatInterval,
                           int scanNotActiveChannelInterval,
                           boolean enableResultCache,
                           int resultCacheExpire,
                           int corePoolSize,
                           int maximumPoolSize,
                           String flowType,
                           int maxConnections,
                           String disuseStrategyType,
                           boolean enableBuffer,
                           int bufferSize,
                           boolean enableRateLimiter,
                           String rateLimiterType,
                           int permits,
                           int milliSeconds,
                           String rateLimiterFailStrategy,
                           boolean enableFusing,
                           String fusingType,
                           double totalFailure,
                           int fusingMilliSeconds) {
        //调用父类构造方法
        super(serverAddress,
                serverRegistryAddress,
                registryAddress,
                registryType,
                registryLoadBalanceType,
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
                rateLimiterFailStrategy,
                enableFusing,
                fusingType,
                totalFailure,
                fusingMilliSeconds);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                ServiceMeta serviceMeta = new ServiceMeta(this.getServiceName(rpcService), rpcService.version(),
                        this.serverRegistryHost, this.serverRegistryPort,rpcService.group(), getWeight(rpcService.weight()));
                handlerMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()), serviceBean);
                try {
                    registryService.register(serviceMeta);
                }catch (Exception e){
                    log.error("rpc server init spring exception:{}", e);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        AsyncStartProviderThreadPool.submit(() -> this.startNettyServer());
    }

    private int getWeight(int weight) {
        if (weight < RpcConstants.SERVICE_WEIGHT_MIN){
            weight = RpcConstants.SERVICE_WEIGHT_MIN;
        }
        if (weight > RpcConstants.SERVICE_WEIGHT_MAX){
            weight = RpcConstants.SERVICE_WEIGHT_MAX;
        }
        return weight;
    }

    /**
     * 获取serviceName
     */
    private String getServiceName(RpcService rpcService){
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == null || clazz == void.class){
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()){
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }

}
