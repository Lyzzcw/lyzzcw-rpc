package lyzzcw.work.rpc.proxy.api.async;

import lyzzcw.work.rpc.proxy.api.future.RpcFuture;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/5 9:37
 * Description: 动态代理异步接口
 */
public interface IAsyncObjectProxy {
    /**
     * 异步代理对象调用方法
     * @param funcName 方法名称
     * @param args 方法参数
     * @return 封装好的RPCFuture对象
     */
    RpcFuture call(String funcName, Object... args);
}
