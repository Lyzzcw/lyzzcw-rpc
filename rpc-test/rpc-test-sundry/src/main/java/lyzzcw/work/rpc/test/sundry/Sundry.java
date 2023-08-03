package lyzzcw.work.rpc.test.sundry;

import lyzzcw.work.rpc.protocol.RpcProtocol;
import lyzzcw.work.rpc.protocol.enums.RpcType;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.protocol.header.RpcHeaderFactory;
import lyzzcw.work.rpc.protocol.request.RpcRequest;
import org.junit.Test;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/3 10:44
 * Description: No Description
 */
public class Sundry {

    @Test
    public void test(){
        RpcHeader header = RpcHeaderFactory.getRequestHeader("jdk", RpcType.REQUEST.getType());
        RpcRequest request = new RpcRequest();
        request.setOneway(false);
        request.setAsync(false);
        request.setClassName("lyzzcw.work.rpc.demo.RpcProtocol");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"lyzzcw"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        RpcProtocol rpcProtocol = new RpcProtocol();
        rpcProtocol.setHeader(header);
        rpcProtocol.setBody(request);
        System.out.println(rpcProtocol);
    }
}
