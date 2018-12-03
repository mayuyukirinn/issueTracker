package cn.edu.fudan.cloneservice;


import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CloneServiceApplicationTests {

    @Value("${resultFileHome}")
    private String resultFileHome;
    @Value("${inner.service.path}")
    private String innerServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;

    @Autowired
    RestTemplate restTemplate;



    private boolean checkOut(String repoId, String commitId) {
        String url=commitServicePath + "/checkout?repo_id=" + repoId + "&commit_id=" + commitId;
        System.out.println(url);
        JSONObject response = restTemplate.getForObject(url, JSONObject.class);
        return response != null && response.getJSONObject("data").getString("status").equals("Successful");
    }

    @Test
    public void testCheckout(){
        System.out.println(checkOut("891cd950-b4eb-11e8-885d-d067e5ea858d","8a5a1c06180ee6840481652ba67a7fbcd5f5be48"));
    }

}
