package lyzzcw.work.rpc.demo.spring.annotation.provider;

import lyzzcw.work.rpc.demo.spring.annotation.provider.config.SpringAnnotationProviderConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/17 14:35
 * Description: No Description
 */
public class ProviderStarter {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(SpringAnnotationProviderConfig.class);
    }
}
