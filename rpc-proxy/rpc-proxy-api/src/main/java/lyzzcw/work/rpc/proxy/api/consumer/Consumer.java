package lyzzcw.work.rpc.proxy.api.consumer;

import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import lyzzcw.work.rpc.proxy.api.future.RpcFuture;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 17:05
 * Description: 服务消费者
 */
public interface Consumer {
    /**
     * 消费者发送 request 请求
     */
    RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception;
}
