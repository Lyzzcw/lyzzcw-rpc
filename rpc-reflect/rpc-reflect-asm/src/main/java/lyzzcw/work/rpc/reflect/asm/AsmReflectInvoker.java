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
package lyzzcw.work.rpc.reflect.asm;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.reflect.api.ReflectInvoker;
import lyzzcw.work.rpc.reflect.asm.proxy.ReflectProxy;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author lzy
 * @version 1.0.0
 * @description ASM反射机制
 */
@SPIClass
@Slf4j
public class AsmReflectInvoker implements ReflectInvoker {

    private final ThreadLocal<Boolean> exceptionThreadLocal = new ThreadLocal<>();

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        if(log.isDebugEnabled()){
            log.info("use asm reflect type invoke method..."  + Thread.currentThread().getName());
        }
        exceptionThreadLocal.set(false);
        Object result = null;
        try{
            Constructor<?> constructor = serviceClass.getConstructor(new Class[]{});
            Object[] constructorParam = new Object[]{};
            Object instance = ReflectProxy.newProxyInstance(AsmReflectInvoker.class.getClassLoader(), getInvocationHandler(serviceBean), serviceClass, constructor, constructorParam);
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            result = method.invoke(instance, parameters);
            if (exceptionThreadLocal.get()){
                throw new RuntimeException("rpc provider throws exception...");
            }
        }finally {
            exceptionThreadLocal.remove();
        }
        return result;
    }

    private InvocationHandler getInvocationHandler(Object obj){
        return (proxy, method, args) -> {
            if(log.isDebugEnabled()){
                log.debug("use proxy invoke method..." + Thread.currentThread().getName());
            }
            method.setAccessible(true);
            Object result = null;
            try{
                result = method.invoke(obj, args);
            }catch (Exception e){
                exceptionThreadLocal.set(true);
            }
            return result;
        };
    }
}
