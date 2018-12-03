package cn.edu.fudan.projectmanager.component;

import cn.edu.fudan.projectmanager.exception.AuthException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
public class RestInterfaceManager {

    @Value("${account.service.path}")
    private String accountServicePath;
    @Value("${project.service.path}")
    private String projectServicePath;
    @Value("${issue.service.path}")
    private String issueServicePath;
    @Value("${scan.service.path}")
    private String scanServicePath;
    @Value("${event.service.path}")
    private String eventServicePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //----------------------------------account service----------------------------------------------------
    public String getAccountId(String userToken){
        Map<String,String> urlParameters=new HashMap<>();
        urlParameters.put("userToken",userToken);
        return restTemplate.getForObject(accountServicePath+"/user/accountId?userToken={userToken}",String.class,urlParameters);
    }

    public void userAuth(String userToken)throws AuthException {
        JSONObject result = restTemplate.getForObject(accountServicePath + "/user/auth/" + userToken, JSONObject.class);
        if (result == null || result.getIntValue("code") != 200) {
            throw new AuthException("auth failed!");
        }
    }

    //--------------------------------project service------------------------------------------------------
    public String getRepoIdOfProject(String projectId) {
        return restTemplate.getForObject(projectServicePath + "/inner/project/repo-id?project-id=" + projectId, String.class);
    }

    //--------------------------------issue service----------------------------------------------------------

    public void deleteIssuesOfRepo(String repoId,String category){
        restTemplate.delete(issueServicePath + "/inner/issue/" +category+"/"+ repoId);
    }

    public void deleteRawIssueOfRepo(String repoId,String category){
        restTemplate.delete(issueServicePath + "/inner/raw-issue/" +category+"/"+ repoId);
    }

    //-------------------------------scan service--------------------------------------------------------------
    public void deleteScanOfRepo(String repoId,String category){
        restTemplate.delete(scanServicePath+"/inner/scan/" +category+"/"+ repoId);
    }

    //-----------------------------event service---------------------------------------------------------------
    public void deleteEventOfRepo(String repoId,String category){
        restTemplate.delete(eventServicePath+"/inner/event/" +category+"/"+ repoId);
    }

}
