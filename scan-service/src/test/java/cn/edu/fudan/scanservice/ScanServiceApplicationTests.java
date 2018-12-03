package cn.edu.fudan.scanservice;

import cn.edu.fudan.scanservice.service.impl.ScanOperationAdapter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = ScanServiceApplication.class)
@TestPropertySource("classpath:application-test.properties")
@PowerMockIgnore({"javax.crypto.*","javax.management.*"})
public class ScanServiceApplicationTests {

    private final static Logger logger = LoggerFactory.getLogger(ScanOperationAdapter.class);

    @Autowired
    private HttpHeaders httpHeaders;

    @Value("${inner.service.path}")
    private String innerServicePath;

    @BeforeClass
    public static void beforeTest() {
        System.out.println("开始测试..................................");
    }

    @AfterClass
    public static void afterTest() {
        System.out.println("结束测试..................................");
    }

//    protected RestTemplate restTemplate;
//
//    @Autowired
//    public void setRestTemplate(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }

//    @Test
//    @Ignore
//    public void contextLoads() {
//        JSONObject requestParam = new JSONObject();
//        requestParam.put("uuid", "9b60eadb-6738-4d73-acaa-114f4c848759");
//        requestParam.put("scan_status", "Not Scanned");
//        HttpEntity<Object> entity = new HttpEntity<>(requestParam, httpHeaders);
//        restTemplate.exchange(innerServicePath + "/inner/project", HttpMethod.PUT, entity, JSONObject.class);
//    }

    @Test
    public void test(){

    }

}
