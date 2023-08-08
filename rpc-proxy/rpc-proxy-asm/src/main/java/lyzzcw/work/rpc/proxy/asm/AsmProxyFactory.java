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
package lyzzcw.work.rpc.proxy.asm;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.proxy.api.BaseProxyFactory;
import lyzzcw.work.rpc.proxy.api.ProxyFactory;
import lyzzcw.work.rpc.proxy.asm.proxy.ASMProxy;
import lyzzcw.work.rpc.spi.annotation.SPIClass;


/**
 * @author lzy
 * @version 1.0.0
 * @description ASM动态代理
 */
@SPIClass
@Slf4j
public class AsmProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<T> clazz) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("based on asm dynamic proxy");
            }
            return (T) ASMProxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, objectProxy);
        } catch (Exception e) {
            log.error("asm proxy throws exception:", e);
        }
        return null;
    }
}
