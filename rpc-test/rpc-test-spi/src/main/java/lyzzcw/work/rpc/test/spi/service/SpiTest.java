package lyzzcw.work.rpc.test.spi.service;

import lyzzcw.work.rpc.serialization.api.Serialization;
import lyzzcw.work.rpc.spi.factory.ExtensionFactory;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;
import org.junit.Test;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/7 9:23
 * Description: No Description
 */
public class SpiTest {
    @Test
    public void test(){
        SpiService spiService = ExtensionLoader.getExtension(SpiService.class,"spiService");
        String result = spiService.hello("lyzzcw");
        System.out.println(result);
    }

    @Test
    public void test1(){
        Serialization spiService = ExtensionLoader.getExtension(Serialization.class,"json");
        byte[] bytes = spiService.serialize(new String("lyzzcw"));
        String result = spiService.deserialize(bytes,String.class);
        System.out.println(result);
    }
}
