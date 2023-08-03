package lyzzcw.work.rpc.test.provider.single;

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
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "lyzzcw.work.rpc.test");
        singleServer.startNettyServer();
    }

}
