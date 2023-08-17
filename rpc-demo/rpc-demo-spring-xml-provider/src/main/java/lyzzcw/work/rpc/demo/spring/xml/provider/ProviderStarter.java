package lyzzcw.work.rpc.demo.spring.xml.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/17 11:20
 * Description: No Description
 */
public class ProviderStarter {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("provider-spring.xml");
    }
}
