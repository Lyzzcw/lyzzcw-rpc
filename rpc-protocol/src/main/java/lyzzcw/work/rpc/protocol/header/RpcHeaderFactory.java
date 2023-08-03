package lyzzcw.work.rpc.protocol.header;


import lyzzcw.work.rpc.common.id.IdFactory;
import lyzzcw.work.rpc.constant.RpcConstants;

/**
 * @author lzy
 * @version 1.0.0
 * @description RpcHeaderFactory
 */
public class RpcHeaderFactory {

    public static RpcHeader getRequestHeader(String serializationType, int messageType){
        RpcHeader header = new RpcHeader();
        header.setMagic(RpcConstants.MAGIC);
        header.setRequestId(IdFactory.getId());
        header.setMsgType((byte) messageType);
        header.setStatus((byte) 0x1);
        header.setSerializationType(serializationType);
        return header;
    }
}