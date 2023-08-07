package lyzzcw.work.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.common.exception.SerializerException;
import lyzzcw.work.rpc.serialization.api.Serialization;
import lyzzcw.work.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/7 16:50
 * Description: Kryo Serialization
 */
@SPIClass
@Slf4j
public class KryoSerialization implements Serialization {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);//支持循环引用
        kryo.setRegistrationRequired(false);//关闭注册行为
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T obj) {
        if(log.isDebugEnabled()) {
            log.debug("execute kryo serialize...");
        }
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryo.writeClassAndObject(output, obj);
        output.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) {
        if(log.isDebugEnabled()){
            log.debug("execute kryo deserialize...");
        }
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        Input input = new Input(byteArrayInputStream);
        input.close();
        return (T) kryo.readClassAndObject(input);
    }
}
