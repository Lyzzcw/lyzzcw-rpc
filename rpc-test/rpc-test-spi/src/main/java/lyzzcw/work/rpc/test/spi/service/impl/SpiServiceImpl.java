package lyzzcw.work.rpc.test.spi.service.impl;

import lyzzcw.work.rpc.spi.annotation.SPIClass;
import lyzzcw.work.rpc.test.spi.service.SpiService;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/7 9:18
 * Description: No Description
 */
@SPIClass
public class SpiServiceImpl implements SpiService {
    @Override
    public String hello(String name) {
        return "hello world :" + name;
    }
}
