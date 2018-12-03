package cn.edu.fudan.scanservice.component;

import cn.edu.fudan.scanservice.exception.AuthException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;


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
    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${repository.service.path}")
    private String repoServicePath;
    @Value("${issue.service.path}")
    private String issueServicePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //----------------------------------account service----------------------------------------------------
    public void userAuth(String userToken)throws AuthException {
        JSONObject result = restTemplate.getForObject(accountServicePath + "/user/auth/" + userToken, JSONObject.class);
        if (result == null || result.getIntValue("code") != 200) {
            throw new AuthException("auth failed!");
        }
    }

    //-----------------------------------commit service-------------------------------------------------------
    public JSONObject checkOut(String repo_id,String commit_id){
        return restTemplate.getForObject(commitServicePath + "/checkout?repo_id=" + repo_id + "&commit_id=" + commit_id, JSONObject.class);
    }
    public JSONObject getCommitTime(String commitId){
        return restTemplate.getForObject(commitServicePath+"/commit-time?commit_id="+commitId,JSONObject.class);
    }

    public JSONObject getCommitsOfRepo(String repoId,Integer page,Integer size){
        return restTemplate.getForObject(commitServicePath + "?repo_id=" + repoId + "&page=" + page + "&per_page=" + size + "&is_whole=true", JSONObject.class);
    }

    //-----------------------------------repo service--------------------------------------------------------
    public JSONObject getRepoById(String repoId){
        return restTemplate.getForObject(repoServicePath + "/" + repoId, JSONObject.class);
    }

    //-----------------------------------issue service-------------------------------------------------------
    public JSONObject mapping(JSONObject requestParam){
        return restTemplate.postForObject(issueServicePath + "/inner/issue/mapping", requestParam, JSONObject.class);
    }

    public void insertRawIssuesWithLocations(List<JSONObject> rawIssues){
        restTemplate.postForObject(issueServicePath+"/inner/raw-issue",rawIssues,JSONObject.class);
    }
    public void deleteRawIssueOfRepo(String repoId,String category){
        restTemplate.delete(issueServicePath + "/inner/raw-issue/" +category+"/"+ repoId);
    }

    //-----------------------------------------------project service-------------------------------------------------
    public String getRepoIdOfProject(String projectId) {
        return restTemplate.getForObject(projectServicePath + "/inner/project/repo-id?project-id=" + projectId, String.class);
    }

    public JSONArray getProjectsOfRepo(String repoId){
        return restTemplate.getForObject(projectServicePath + "/inner/project?repo_id=" + repoId,JSONArray.class);
    }

    public void updateProject(JSONObject projectParam) {
        try {
            restTemplate.put(projectServicePath + "/inner/project", projectParam, JSONObject.class);
        } catch (Exception e) {
            throw new RuntimeException("project update failed!");
        }
    }

}
