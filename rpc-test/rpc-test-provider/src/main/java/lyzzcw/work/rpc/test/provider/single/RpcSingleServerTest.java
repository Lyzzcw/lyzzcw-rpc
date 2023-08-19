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

    public static void main(String[] args) {
        RpcSingleServer singleServer = new RpcSingleServer(
                "0.0.0.0:27880",
                "127.0.0.1:27880",
//                "127.0.0.1:2181",
                "127.0.0.1:8848?namespace=1167c87c-5ea8-45b5-90d6-7964d78abe4a",
                RpcConstants.REGISTRY_CENTER_NACOS,
                RpcConstants.SERVICE_LOAD_BALANCER_ROUND_ROBIN,
                "lyzzcw.work.rpc.test",
                RpcConstants.REFLECT_TYPE_ASM,
                10000,-1,
                true,
                100000,
                4,
                4,
                RpcConstants.FLOW_POST_PROCESSOR_PRINT,
                200,
                RpcConstants.RPC_CONNECTION_DISUSE_STRATEGY_DEFAULT);
        singleServer.startNettyServer();
    }

//    @Test
//    public void startRpcSingleServer(){
//        RpcSingleServer singleServer = new RpcSingleServer(
//                "127.0.0.1:27880",
////                "127.0.0.1:2181",
//                "127.0.0.1:8848?namespace=1167c87c-5ea8-45b5-90d6-7964d78abe4a",
//                RpcConstants.REGISTRY_CENTER_NACOS,
//                RpcConstants.SERVICE_LOAD_BALANCER_ROUND_ROBIN,
//                "lyzzcw.work.rpc.test",
//                RpcConstants.REFLECT_TYPE_ASM,-1,-1);
//        singleServer.startNettyServer();
//    }

}
