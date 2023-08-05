package lyzzcw.work.rpc.proxy.jdk;

import lyzzcw.work.rpc.proxy.api.BaseProxyFactory;
import lyzzcw.work.rpc.proxy.api.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 17:29
 * Description: JDK动态代理工厂类
 */
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                objectProxy
        );
    }
}
