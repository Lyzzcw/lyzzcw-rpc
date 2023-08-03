package lyzzcw.work.rpc.provider.common.server.api;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/7/25 14:25
 * Description: 启动RPC服务的接口
 */
public interface Server {
    /**
     * 启动Netty服务
     */
    void startNettyServer();
}
