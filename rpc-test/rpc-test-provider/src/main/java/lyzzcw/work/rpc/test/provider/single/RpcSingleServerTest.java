package lyzzcw.work.rpc.test.provider.single;

import lyzzcw.work.rpc.constant.RpcConstants;
import lyzzcw.work.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/3 10:01
 * Description: 测试Java原生启动RPC
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer(){
        RpcSingleServer singleServer = new RpcSingleServer(
                "127.0.0.1:27880",
                "127.0.0.1:2181",
                RpcConstants.REGISTRY_CENTER_ZOOKEEPER,
                RpcConstants.SERVICE_LOAD_BALANCER_ROUND_ROBIN,
                "lyzzcw.work.rpc.test",
                RpcConstants.REFLECT_TYPE_ASM);
        singleServer.startNettyServer();
    }

}
