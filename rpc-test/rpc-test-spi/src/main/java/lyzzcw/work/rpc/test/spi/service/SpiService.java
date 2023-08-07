package lyzzcw.work.rpc.test.spi.service;

import lyzzcw.work.rpc.spi.annotation.SPI;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/7 9:18
 * Description: No Description
 */
@SPI("spiService")
public interface SpiService {
    String hello(String name);
}
