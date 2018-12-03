package cn.edu.fudan.eventservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = EventServiceApplication.class)
@ActiveProfiles({"test"})
@PowerMockIgnore({"javax.crypto.*","javax.management.*"})
public class EventServiceApplicationTests {

    @Test
    public void contextLoads() {
    }

}
