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
package lyzzcw.work.rpc.proxy.api.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lyzzcw.work.rpc.proxy.api.consumer.Consumer;
import lyzzcw.work.rpc.registry.api.RegistryService;

import java.io.Serializable;

/**
 * @author lzy
 * @version 1.0.0
 * @description 代理配置类
 */
@Data
@NoArgsConstructor
public class ProxyConfig<T> implements Serializable {
    private static final long serialVersionUID = 6648940252795742398L;

    /**
     * 接口的Class实例
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
     * 超时时间
     */
    private long timeout;
    /**
     * 服务注册与发现接口
     */
    private RegistryService registryService;
    /**
     * 消费者接口
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

    public ProxyConfig(Class<T> clazz, String serviceVersion, String serviceGroup,
                       long timeout,RegistryService registryService, Consumer consumer,
                       String serializationType, boolean async, boolean oneway) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.registryService = registryService;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
    }
}
