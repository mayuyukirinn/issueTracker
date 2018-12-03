import cn.edu.fudan.tagservice.domain.Priority;
import org.junit.Test;

import java.util.UUID;

public class tagServiceTest {

    @Test
    public void test() {
        Priority priority = Priority.getByValue("High");

        System.out.println(priority.toString());
        System.out.println(priority.getColor());

        System.out.println(UUID.randomUUID().toString());
    }
}
