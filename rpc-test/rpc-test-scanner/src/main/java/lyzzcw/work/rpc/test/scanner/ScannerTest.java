package lyzzcw.work.rpc.test.scanner;

import lyzzcw.work.rpc.common.scanner.ClassScanner;
import lyzzcw.work.rpc.common.scanner.reference.RpcReferenceScanner;
import org.junit.Test;

import java.util.List;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/7/25 9:37
 * Description: No Description
 */
public class ScannerTest {
    /**
     * 扫描lyzzcw.work.rpc.test.scanner包下所有的类
     */
    @Test
    public void testScannerClassNameList() throws Exception {
        List<String> classNameList = ClassScanner.getClassNameList("lyzzcw.work.rpc.test.scanner", true);
        classNameList.forEach(System.out::println);
    }

    /**
     * 扫描lyzzcw.work.rpc.test.scanner包下所有标注了@RpcReference注解的类
     */
    @Test
    public void testScannerClassNameListByRpcReference() throws Exception {
        RpcReferenceScanner.doScannerWithRpcReferenceAnnotationFilter("lyzzcw.work.rpc.test.scanner");
    }
}
