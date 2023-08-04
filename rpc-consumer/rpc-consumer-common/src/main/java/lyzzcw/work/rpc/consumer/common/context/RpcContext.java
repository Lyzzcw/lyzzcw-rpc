package lyzzcw.work.rpc.consumer.common.context;

import lyzzcw.work.rpc.proxy.api.future.RpcFuture;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 15:06
 * Description: 保存RPC上下文
 */
public class RpcContext {
    private RpcContext(){

    }

    /**
     * RpcContext实例
     */
    private static final RpcContext AGENT = new RpcContext();

    /**
     * 存放RpcFuture的InheritableThreadLocal
     */
    private static final InheritableThreadLocal<RpcFuture> RPC_FUTURE_INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 获取上下文
     * @return RPC服务的上下文信息
     */
    public static RpcContext getContext(){
        return AGENT;
    }

    /**
     * 将RpcFuture保存到线程的上下文
     * @param rpcFuture
     */
    public void setRpcFuture(RpcFuture rpcFuture){
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.set(rpcFuture);
    }

    /**
     * 获取RpcFuture
     */
    public RpcFuture getRpcFuture(){
        return RPC_FUTURE_INHERITABLE_THREAD_LOCAL.get();
    }

    /**
     * 移除RpcFuture
     */
    public void removeRpcFuture(){
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.remove();
    }
}
