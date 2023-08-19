package lyzzcw.work.rpc.disuse.api;

import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.disuse.api.connection.ConnectionInfo;
import lyzzcw.work.rpc.spi.annotation.SPI;

import java.util.List;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/19 14:57
 * Description: 淘汰策略
 */
@SPI(RpcConstants.RPC_CONNECTION_DISUSE_STRATEGY_DEFAULT)
public interface DisuseStrategy {

    /**
     * 从连接列表中根据规则获取一个连接对象
     */
    ConnectionInfo selectConnection(List<ConnectionInfo> connectionList);
}