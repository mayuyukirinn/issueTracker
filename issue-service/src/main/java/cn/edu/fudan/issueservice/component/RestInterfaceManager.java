package cn.edu.fudan.issueservice.component;

import cn.edu.fudan.issueservice.exception.AuthException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
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
    @Value("${code.service.path}")
    private String codeServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${tag.service.path}")
    private String tagServicePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //------------------------------------------------account service-----------------------------------------------------------
    public String getAccountId(String userToken){
        Map<String,String> urlParameters=new HashMap<>();
        urlParameters.put("userToken",userToken);
        return restTemplate.getForObject(accountServicePath+"/user/accountId?userToken={userToken}",String.class,urlParameters);
    }

    public void userAuth(String userToken)throws AuthException{
        JSONObject result = restTemplate.getForObject(accountServicePath + "/user/auth/" + userToken, JSONObject.class);
        if (result == null || result.getIntValue("code") != 200) {
            throw new AuthException("auth failed!");
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getAccountIds() {
        return restTemplate.getForObject(accountServicePath + "/user/accountIds", List.class);
    }

    //------------------------------------------------end----------------------------------------------------------------------


    //----------------------------------------------tag service---------------------------------------------------------------
    public void deleteTagsOfIssueInOneRepo(List<String> issueIds){
        if (issueIds != null && !issueIds.isEmpty()) {
            JSONObject response = restTemplate.postForObject(tagServicePath + "/tagged-delete", issueIds, JSONObject.class);
            if (response == null || response.getIntValue("code") != 200) {
                throw new RuntimeException("tag item delete failed!");
            }
        }
    }

    public JSONArray getTagsOfIssue(String issueId){
        return  restTemplate.getForObject(tagServicePath + "?item_id=" + issueId, JSONArray.class);
    }

    public JSONArray getSpecificTaggedIssueIds(String ...tagIds){
        return restTemplate.postForObject(tagServicePath + "/item-ids", tagIds, JSONArray.class);
    }
    public JSONArray getSpecificTaggedIssueIds(JSONArray tagIds){
        return restTemplate.postForObject(tagServicePath + "/item-ids", tagIds, JSONArray.class);
    }

    public JSONArray getSolvedIssueIds(List<String> tag_ids){
        return restTemplate.postForObject(tagServicePath + "/item-ids", tag_ids, JSONArray.class);
    }

    public void modifyTags(List<JSONObject> tags){
        restTemplate.postForObject(tagServicePath+"/tagged-modify", tags, JSONObject.class);
    }

    public void addTags(List<JSONObject> tags){
        restTemplate.postForObject(tagServicePath,tags, JSONObject.class);
    }


    //----------------------------------------------------end--------------------------------------------------------

    //-----------------------------------------------project service-------------------------------------------------
    public JSONArray getRepoIdsOfAccount(String account_id,String type) {
        return restTemplate.getForObject(projectServicePath + "/inner/project/repo-ids?account_id=" + account_id+"&type="+type, JSONArray.class);
    }

    public String getRepoIdOfProject(String projectId) {
        return restTemplate.getForObject(projectServicePath + "/inner/project/repo-id?project-id=" + projectId, String.class);
    }

    public JSONArray getProjectList(String account_id) {
        return restTemplate.getForObject(projectServicePath + "/inner/projects?account_id=" + account_id,JSONArray.class);
    }
    //-------------------------------------------------end-------------------------------------------------------------

    //---------------------------------------------commit service------------------------------------------------------

    public JSONObject getOneCommitByCommitId(String commitId){
        return restTemplate.getForObject(commitServicePath+"/"+commitId,JSONObject.class);
    }

    public JSONObject checkOut(String repo_id,String commit_id){
        return restTemplate.getForObject(commitServicePath + "/checkout?repo_id=" + repo_id + "&commit_id=" + commit_id, JSONObject.class);
    }

    public JSONObject getCommitTime(String commitId){
        return restTemplate.getForObject(commitServicePath+"/commit-time?commit_id="+commitId,JSONObject.class);
    }
    //----------------------------------------------end-----------------------------------------------------------------

    //---------------------------------------------code service---------------------------------------------------------
    public JSONObject getCode(String filePath){
        return restTemplate.getForObject(codeServicePath + "?file_path=" + filePath, JSONObject.class);
    }

}
