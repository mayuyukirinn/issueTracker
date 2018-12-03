package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.IssueServiceApplicationTests;
import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.domain.IssueCount;
import cn.edu.fudan.issueservice.domain.RawIssue;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.impl.BaseMappingServiceImpl;
import cn.edu.fudan.issueservice.service.impl.IssueServiceImpl;
import cn.edu.fudan.issueservice.util.TestDataMaker;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.ListMap;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@PrepareForTest({IssueService.class, IssueServiceImpl.class, IssueDao.class, RawIssueDao.class,RestTemplate.class, StringRedisTemplate.class,HashOperations.class, ResponseEntity.class, ListOperations.class, BaseMappingServiceImpl.class})
public class IssueServiceTest extends IssueServiceApplicationTests {

    @Value("${tag.service.path}")
    private String tagServicePath;
    @Value("${solved.tag_id}")
    private String solvedTagId;
    @Value("${ignore.tag_id}")
    private String ignoreTagId;
    @Value("${inner.service.path}")
    private String innerServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;


    @Autowired
    private RawIssueDao rawIssueDao;

    @Mock
    private IssueDao issueDao;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private HashOperations hashOperations;

    @Mock
    private ListOperations listOperations;

    @Mock
    private ResponseEntity responseEntity;

    @Mock
    private ResponseEntity accountIdResponseEntity;

    @Mock
    private ResponseEntity repoIdResponseEntity;

    @Mock
    private BaseMappingServiceImpl baseMappingService;


    @Autowired
    @InjectMocks
    private IssueService issueService = new IssueServiceImpl();

    private TestDataMaker testDataMaker;

    List<Issue> list;
    Issue issue1;
    Issue issue2;

    List<RawIssue> rawIssues1;
    List<RawIssue> rawIssues2;
    RawIssue rawIssue1;
    RawIssue rawIssue2;
    RawIssue rawIssue3;

    Map<String, Object> map;

    @Before
    public void setup() throws Exception {
        rawIssueDao = Mockito.mock(RawIssueDao.class);
        testDataMaker = new TestDataMaker();

        issue1 = testDataMaker.issueMaker1();
        issue2 = testDataMaker.issueMaker2();
        list = new ArrayList<>();
        list.add(issue1);
        list.add(issue2);
        rawIssue1 = testDataMaker.rawIssueMaker1();
        rawIssue2 = testDataMaker.rawIssueMaker2();
        rawIssue3 = testDataMaker.rawIssueMaker3();
        rawIssues1 = new ArrayList<>();
        rawIssues1.add(rawIssue1);
        rawIssues2 = new ArrayList<>();
        rawIssues2.add(rawIssue2);
        rawIssues2.add(rawIssue3);
        MemberModifier.field(IssueServiceImpl.class, "issueDao").set(issueService, issueDao);
        MemberModifier.field(IssueServiceImpl.class, "restTemplate").set(issueService, restTemplate);
        MemberModifier.field(IssueServiceImpl.class, "stringRedisTemplate").set(issueService, stringRedisTemplate);
        System.out.println("finish mocking");



        map = new HashMap<String, Object>();
        map.put("project_id", "222");
        map.put("type", "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE");
        map.put("start", 0);
        map.put("size", 2);
        map.put("page", 1);

    }

    @Test
    public void insertIssueList() {
        doNothing().when(issueDao).insertIssueList(list);
        issueService.insertIssueList(list);
        verify(issueDao, Mockito.atLeastOnce()).insertIssueList(list);
    }

    @Test
    public void deleteIssueByProjectId() {
        String repo_id = "repo_id";
        List<String> stringList = new ArrayList<String>();
        stringList.add("iss1");
        stringList.add("iss2");
        PowerMockito.when(issueDao.getIssueIdsByRepoIdAndCategory(repo_id,"bug")).thenReturn(stringList);
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(401);
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSON(responseBean).toString());
        /*
            当返回的JSONObject为null
         */
        PowerMockito.when(restTemplate.postForObject(tagServicePath+"/tagged-delete",stringList, JSONObject.class)).thenReturn(null);
        try{
            issueService.deleteIssueByRepoIdAndCategory(repo_id,"bug");
        }catch(RuntimeException e){
            Assert.assertEquals("tag item delete failed!",e.getMessage());
        }
        /*
            当返回的JSONObject的code代码不是200
         */
        PowerMockito.when(restTemplate.postForObject(tagServicePath+"/tagged-delete",stringList, JSONObject.class)).thenReturn(jsonObject);
        try{
            issueService.deleteIssueByRepoIdAndCategory(repo_id,"bug");
        }catch(RuntimeException e){
            Assert.assertEquals("tag item delete failed!",e.getMessage());
        }
        /*
            当返回的JSONObject的code代码是200
         */
        responseBean.setCode(200);
        jsonObject = JSONObject.parseObject(JSONObject.toJSON(responseBean).toString());
        PowerMockito.when(restTemplate.postForObject(tagServicePath+"/tagged-delete",stringList, JSONObject.class)).thenReturn(jsonObject);
        issueService.deleteIssueByRepoIdAndCategory(repo_id,"bug");
        verify(issueDao,Mockito.atLeastOnce()).deleteIssueByRepoIdAndCategory(repo_id,"bug");

    }

    @Test
    public void batchUpdateIssue() {
        doNothing().when(issueDao).batchUpdateIssue(list);
        issueService.batchUpdateIssue(list);
        verify(issueDao, Mockito.atLeastOnce()).batchUpdateIssue(list);
    }

    @Test
    public void getIssueByID() {
        String issueId = "iss1";
        PowerMockito.when(issueDao.getIssueByID(issueId)).thenReturn(issue1);
        Issue issue = issueService.getIssueByID(issueId);
        Assert.assertEquals(issue1.getUuid(),issue.getUuid());
    }

    @Test
    public void getIssueList() {
        int count = 2;
        List<String> itemIds = new ArrayList<>();
        itemIds.add("item1");
        itemIds.add("item2");
        JSONArray solved_issue_ids = JSON.parseArray(JSONObject.toJSON(itemIds).toString());

        String repo_id = "repo_id";
        String projectId = "pro1";
        Integer page = 1;
        Integer size = 2;
        String category = "category";
        List<String> tag_ids = new ArrayList<>();
        tag_ids.add(solvedTagId);
        tag_ids.add(ignoreTagId);
        JSONArray tags = new JSONArray();

        PowerMockito.when(restTemplate.getForObject(tagServicePath + "?item_id=" + list.get(1).getUuid(), JSONArray.class)).thenReturn(tags);
        PowerMockito.when(restTemplate.postForObject(tagServicePath+"/item-ids",tag_ids, JSONArray.class)).thenReturn(solved_issue_ids);
        MemberModifier.stub(MemberMatcher.method(IssueServiceImpl.class, "getRepoId")).toReturn(repo_id);
        PowerMockito.when(issueDao.getIssueCount(any(Map.class))).thenReturn(count);
        PowerMockito.when(issueDao.getIssueList(any(Map.class))).thenReturn(list);
        PowerMockito.when(restTemplate.getForObject(tagServicePath + "?item_id=" + list.get(0).getUuid(), JSONArray.class)).thenReturn(tags);
        PowerMockito.when(restTemplate.getForObject(tagServicePath + "?item_id=" + list.get(1).getUuid(), JSONArray.class)).thenReturn(tags);

        Map<String, Object> result = (Map<String, Object>) issueService.getIssueList(projectId, page, size,category);
        Assert.assertEquals(1,result.get("totalPage"));
        Assert.assertEquals(2,result.get("totalCount"));
        List<Issue> listResult = (List<Issue>)result.get("issueList");
        Assert.assertEquals(list.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(list.get(i),listResult.get(i));
        }
    }

    @Test
    public void getFilteredIssueList() {
        int count = 2;
        String project_id = "project_id";
        String repo_id = "repo_id";
        int size = 2;
        String category = "category";
        JSONObject requestParam = new JSONObject();
        requestParam.put("project_id",project_id);
        requestParam.put("size",size);
        requestParam.put("category",category);

        MemberModifier.stub(MemberMatcher.method(IssueServiceImpl.class, "getRepoId")).toReturn(repo_id);

        String issueId1= "issueId1";
        String issueId2= "issueId2";
        List<String> listIssueIds = new ArrayList<String>();
        listIssueIds.add(issueId1);
        listIssueIds.add(issueId2);
        JSONArray IssueIds = JSON.parseArray(JSONObject.toJSON(listIssueIds).toString());

        Map<String, Object> result;
        JSONArray tags = new JSONArray();
        /*
           测试一：当page参数缺失时报错
         */
        try{
            result = (Map<String, Object>)issueService.getFilteredIssueList(requestParam);
        }catch(IllegalArgumentException e){
            Assert.assertEquals("param lost!",e.getMessage());
        }
        //参数补充完全
        int page = 1;
        requestParam.put("page",page);
        /*
            测试二：当tag的筛选条件不为空，且tag的筛选结果为空时
         */
        String tagId1= "tagId1";
        String tagId2= "tagId2";
        List<String> listTagIds = new ArrayList<String>();
        listTagIds.add(tagId1);
        listTagIds.add(tagId2);
        requestParam.put("tags",listTagIds);
        JSONArray tag_ids = JSON.parseArray(JSONObject.toJSON(listTagIds).toString());
        PowerMockito.when(restTemplate.postForObject(tagServicePath + "/item-ids", tag_ids,JSONArray.class)).thenReturn(null);
        result = (Map<String, Object>)issueService.getFilteredIssueList(requestParam);
        Assert.assertEquals(0,result.get("totalPage"));
        Assert.assertEquals(0,result.get("totalCount"));
        Assert.assertEquals(Collections.emptyList(),result.get("issueList"));
        /*
            测试三：当tag的筛选条件不为空，tag的筛选结果不为空时，且type的筛选条件不为空
         */
        String type1= "type1";
        String type2= "type2";
        List<String> listTypes = new ArrayList<String>();
        listTypes.add(type1);
        listTypes.add(type2);
        requestParam.put("types",listTypes);
        PowerMockito.when(restTemplate.postForObject(tagServicePath + "/item-ids", tag_ids,JSONArray.class)).thenReturn(IssueIds);
        PowerMockito.when(issueDao.getIssueCount(any(Map.class))).thenReturn(count);
        PowerMockito.when(issueDao.getIssueList(any(Map.class))).thenReturn(list);
        PowerMockito.when(restTemplate.getForObject(tagServicePath + "?item_id=" + list.get(0).getUuid(), JSONArray.class)).thenReturn(tags);
        PowerMockito.when(restTemplate.getForObject(tagServicePath + "?item_id=" + list.get(1).getUuid(), JSONArray.class)).thenReturn(tags);

        result = (Map<String, Object>)issueService.getFilteredIssueList(requestParam);
        Assert.assertEquals(1,result.get("totalPage"));
        Assert.assertEquals(2,result.get("totalCount"));
        List<Issue> listResult = (List<Issue>)result.get("issueList");
        Assert.assertEquals(list.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(list.get(i),listResult.get(i));
        }
    }

    @Test
    public void getDashBoardInfo() {
        String repoId1 = "repoId1";
        String repoId2= "repoId2";
        String duration = "duration";
        String project_id = "proId";
        String userToken = "token";
        String category = "category";
        String account_id = "acc_id";
        MemberModifier.stub(MemberMatcher.method(IssueServiceImpl.class, "getAccountId")).toReturn(account_id);

        List<String> listRepoIds = new ArrayList<String>();
        listRepoIds.add(repoId1);
        listRepoIds.add(repoId2);
        JSONArray repoIds = JSON.parseArray(JSONObject.toJSON(listRepoIds).toString());
        MemberModifier.stub(MemberMatcher.method(IssueServiceImpl.class, "getRepoId")).toReturn(repoId1);
        PowerMockito.when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        PowerMockito.when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        Object newIssueCount = "1";
        Object eliminatedIssueCount = "2";
        Object remainingIssueCount = "3";
        PowerMockito.when(hashOperations.get("dashboard:"+category+":"+ duration + ":" + repoId1, "new")).thenReturn(newIssueCount);
        PowerMockito.when(hashOperations.get("dashboard:"+category+":"+ duration + ":" + repoId1, "remaining")).thenReturn(remainingIssueCount);
        PowerMockito.when(hashOperations.get("dashboard:"+category+":"+ duration + ":" + repoId1, "eliminated")).thenReturn(eliminatedIssueCount);
        PowerMockito.when(listOperations.range("dashboard:"+category+":"+duration+":new"+ repoId1,0,-1)).thenReturn(null);
        PowerMockito.when(hashOperations.get("dashboard:"+category+":"+ duration + ":" + repoId2, "new")).thenReturn(newIssueCount);
        PowerMockito.when(hashOperations.get("dashboard:"+category+":"+ duration + ":" + repoId2, "remaining")).thenReturn(remainingIssueCount);
        PowerMockito.when(hashOperations.get("dashboard:"+category+":"+ duration + ":" + repoId2, "eliminated")).thenReturn(eliminatedIssueCount);
        PowerMockito.when(listOperations.range("dashboard:"+category+":"+duration+":new"+ repoId2,0,-1)).thenReturn(null);
        /*
            当project_id为null时，且当用户没有添加project时
         */
        MemberModifier.stub(MemberMatcher.method(IssueServiceImpl.class, "getRepoIds")).toReturn(null);
        IssueCount result = (IssueCount)issueService.getDashBoardInfo(duration,null,userToken,category);
        Assert.assertEquals(0,result.getNewIssueCount());
        Assert.assertEquals(0,result.getEliminatedIssueCount());
        Assert.assertEquals(0,result.getRemainingIssueCount());
        /*
             当project_id为null时，且当用户有添加project时
         */
        MemberModifier.stub(MemberMatcher.method(IssueServiceImpl.class, "getRepoIds")).toReturn(repoIds);
        IssueCount resultAddProject = (IssueCount)issueService.getDashBoardInfo(duration,null,userToken,category);
        Assert.assertEquals(2,resultAddProject.getNewIssueCount());
        Assert.assertEquals(4,resultAddProject.getEliminatedIssueCount());
        Assert.assertEquals(6,resultAddProject.getRemainingIssueCount());
        /*
             当project_id为null时，且当用户有添加project时
         */
        MemberModifier.stub(MemberMatcher.method(IssueServiceImpl.class, "getRepoIds")).toReturn(repoIds);
        IssueCount resultProjectIsNull = (IssueCount)issueService.getDashBoardInfo(duration,project_id,userToken,category);
        Assert.assertEquals(1,resultProjectIsNull.getNewIssueCount());
        Assert.assertEquals(2,resultProjectIsNull.getEliminatedIssueCount());
        Assert.assertEquals(3,resultProjectIsNull.getRemainingIssueCount());
    }

    @Test
    public void getStatisticalResults() {
        /*
            当month为1，且project_id为null
         */
        IssueCount issueCount1 = new IssueCount();
        issueCount1.setNewIssueCount(1);
        issueCount1.setRemainingIssueCount(3);
        issueCount1.setEliminatedIssueCount(2);
        IssueCount issueCount2 = new IssueCount();
        issueCount2.setNewIssueCount(2);
        issueCount2.setRemainingIssueCount(2);
        issueCount2.setEliminatedIssueCount(3);
        IssueCount issueCount3 = new IssueCount();
        issueCount3.setNewIssueCount(3);
        issueCount3.setRemainingIssueCount(1);
        issueCount3.setEliminatedIssueCount(4);
        List<IssueCount> issueCounts = new ArrayList<>();
        issueCounts.add(issueCount1);
        issueCounts.add(issueCount2);
        issueCounts.add(issueCount3);

        Map map = new ListMap();
        map.put("data",issueCounts);
        List<String> newList = new ArrayList<>();
        newList.add("1");
        newList.add("2");
        newList.add("3");
        List<String> remainingList = new ArrayList<>();
        remainingList.add("3");
        remainingList.add("2");
        remainingList.add("1");
        List<String> eliminatedList = new ArrayList<>();
        eliminatedList.add("2");
        eliminatedList.add("3");
        eliminatedList.add("4");
        Integer month = 1;
        String project_id = "project_id";
        String userToken = "userToken";
        String category = "category";
        String account_id = "account_id";
        PowerMockito.when(restTemplate.exchange(eq(innerServicePath + "/user/accountId?userToken=" + userToken), eq(HttpMethod.GET), any(HttpEntity.class),eq(String.class) )).thenReturn(accountIdResponseEntity);
        PowerMockito.when(accountIdResponseEntity.getBody()).thenReturn(account_id);
        PowerMockito.when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        PowerMockito.when(listOperations.range("trend:"+category+":day:new:" + account_id, 0, -1)).thenReturn(newList);
        PowerMockito.when(listOperations.range("trend:"+category+":day:remaining:" + account_id, 0, -1)).thenReturn(remainingList);
        PowerMockito.when(listOperations.range("trend:"+category+":day:eliminated:" + account_id, 0, -1)).thenReturn(eliminatedList);
        Map resultMap = (Map) issueService.getStatisticalResults(month,null,userToken,category);
        List<IssueCount> listResult = (List<IssueCount>)resultMap.get("data");
        Assert.assertEquals(issueCounts.size(),listResult.size());
        for(int i=0;i<listResult.size();++i){
            Assert.assertEquals(issueCounts.get(i).toString(),listResult.get(i).toString());
        }

        /*
            当month不为1，且project_id为null
         */

        month = 3;
        PowerMockito.when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        PowerMockito.when(listOperations.range("trend:"+category+":week:new:" + account_id, 0, -1)).thenReturn(newList);
        PowerMockito.when(listOperations.range("trend:"+category+":week:remaining:" + account_id, 0, -1)).thenReturn(remainingList);
        PowerMockito.when(listOperations.range("trend:"+category+":week:eliminated:" + account_id, 0, -1)).thenReturn(eliminatedList);

        resultMap = (Map) issueService.getStatisticalResults(month,null,userToken,category);
        listResult = (List<IssueCount>)resultMap.get("data");
        Assert.assertEquals(issueCounts.size(),listResult.size());
        for(int i=0;i<listResult.size();++i){
            Assert.assertEquals(issueCounts.get(i).toString(),listResult.get(i).toString());
        }

        /*
            当month不为1，且project_id为null
         */
        String repoId = "repoId";
        month = 1;
        PowerMockito.when(restTemplate.exchange(eq(innerServicePath + "/inner/project/repo-id?project-id=" + project_id), eq(HttpMethod.GET), any(HttpEntity.class),eq(String.class) )).thenReturn(repoIdResponseEntity);
        PowerMockito.when(repoIdResponseEntity.getBody()).thenReturn(repoId);
        PowerMockito.when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        PowerMockito.when(listOperations.range("trend:"+category+":day:new:" + account_id + ":" + repoId, 0, -1)).thenReturn(newList);
        PowerMockito.when(listOperations.range("trend:"+category+":day:remaining:" + account_id + ":" + repoId, 0, -1)).thenReturn(remainingList);
        PowerMockito.when(listOperations.range("trend:"+category+":day:eliminated:" + account_id + ":" + repoId, 0, -1)).thenReturn(eliminatedList);
        resultMap = (Map) issueService.getStatisticalResults(month,project_id,userToken,category);
        listResult = (List<IssueCount>)resultMap.get("data");
        Assert.assertEquals(issueCounts.size(),listResult.size());
        for(int i=0;i<listResult.size();++i){
            Assert.assertEquals(issueCounts.get(i).toString(),listResult.get(i).toString());
        }
        /*
            当month不为1，且project_id为null
         */
        month = 3;
        PowerMockito.when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        PowerMockito.when(listOperations.range("trend:"+category+":week:new:" + account_id + ":" + repoId, 0, -1)).thenReturn(newList);
        PowerMockito.when(listOperations.range("trend:"+category+":week:remaining:" + account_id + ":" + repoId, 0, -1)).thenReturn(remainingList);
        PowerMockito.when(listOperations.range("trend:"+category+":week:eliminated:" + account_id + ":" + repoId, 0, -1)).thenReturn(eliminatedList);
        resultMap = (Map) issueService.getStatisticalResults(month,project_id,userToken,category);
        listResult = (List<IssueCount>)resultMap.get("data");
        Assert.assertEquals(issueCounts.size(),listResult.size());
        for(int i=0;i<listResult.size();++i){
            Assert.assertEquals(issueCounts.get(i).toString(),listResult.get(i).toString());
        }
    }

    @Test
    public void getExistIssueTypes() {
        String category = "category";
        String type1 = "EI_EXPOSE_REP";
        String type2 = "EI_EXPOSE_REP2";
        List<String> types = new ArrayList<String>();
        types.add(type1);
        types.add(type2);
        PowerMockito.when(issueDao.getExistIssueTypes(category)).thenReturn(types);

        List<String> resultTypes = (List<String>)issueService.getExistIssueTypes(category);
        Assert.assertEquals(types.size(),resultTypes.size());
        for (int i = 0; i < resultTypes.size(); ++i) {
            Assert.assertEquals(types.get(i),resultTypes.get(i));
        }

    }


    /*
        只验证了bugmapping
     */
    @Test
    @Ignore
    public void startMapping() {
        String repo_id = "repo_id";
        String category = "bug";
        String pre_commit_id ;
        String current_commit_id ;
        MemberModifier.stub(MemberMatcher.method(BaseMappingServiceImpl.class, "dashboardUpdate")).toReturn(Void.TYPE);
        PowerMockito.when(restTemplate.getForObject(anyString(),eq(JSONObject.class))).thenReturn(null);
        List<String> issueIds =new ArrayList<>();
        issueIds.add("iss1");
        issueIds.add("iss2");


        /*
            当project第一次扫描，且当前版本没有raw issue,且无commitinfo
         */
        pre_commit_id = current_commit_id = "commit_id";
        MemberModifier.stub(MemberMatcher.method(RawIssueDao.class, "getRawIssueByCommitIDAndCategory")).toReturn(Collections.emptyList());
        issueService.startMapping(repo_id,pre_commit_id,current_commit_id,category);
        verify(rawIssueDao,Mockito.never()).batchUpdateIssueId(any(List.class));
        /*
            当project第一次扫描，且当前版本有raw issue
         */
        MemberModifier.stub(MemberMatcher.method(RawIssueDao.class, "getRawIssueByCommitIDAndCategory")).toReturn(rawIssues1);
        doNothing().when(rawIssueDao).batchUpdateIssueId(rawIssues1);
        doNothing().when(issueDao).insertIssueList(any(List.class));
        issueService.startMapping(repo_id,pre_commit_id,current_commit_id,category);
        verify(rawIssueDao,Mockito.times(1)).batchUpdateIssueId(any(List.class));
        verify(issueDao,Mockito.times(1)).insertIssueList(any(List.class));
        /*
            当project不是第一次扫描，且current commit无raw issue
         */
        pre_commit_id = "pre_commit_id";
        current_commit_id = "current_commit_id";
        MemberModifier.stub(MemberMatcher.method(RawIssueDao.class, "getRawIssueByCommitIDAndCategory")).toReturn(Collections.emptyList());
        issueService.startMapping(repo_id,pre_commit_id,current_commit_id,category);
        verify(rawIssueDao,Mockito.times(1)).batchUpdateIssueId(any(List.class));
        /*
            当project不是第一次扫描,且current commit有raw issue
         */
        Calendar calendar=Calendar.getInstance();
        calendar.set(2015, 10, 12,11,32,52);
        Date start_commit_time=calendar.getTime();
        JSONObject startCommit = new JSONObject();
        startCommit.put("commit_time",start_commit_time);
        calendar.set(2015, 10, 12,12,32,52);
        Date end_commit_time=calendar.getTime();
        JSONObject endCommit = new JSONObject();
        endCommit.put("commit_time",end_commit_time);
        JSONArray commits = new JSONArray();
        commits.add(startCommit);
        commits.add(endCommit);

        calendar.set(2015, 10, 12,10,32,52);
        Date commit_time=calendar.getTime();
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("commit_time",commit_time);
        jsonObject.put("data",data);

        PowerMockito.when(rawIssueDao.getRawIssueByCommitIDAndCategory(category,pre_commit_id)).thenReturn(rawIssues1);
        PowerMockito.when(rawIssueDao.getRawIssueByCommitIDAndCategory(category,current_commit_id)).thenReturn(rawIssues2);
        PowerMockito.when(issueDao.getIssueByID(rawIssues1.get(0).getIssue_id())).thenReturn(issue1);
        //PowerMockito.when(issueDao.getSolvedIssueIds(repo_id,pre_commit_id)).thenReturn(issueIds);

        PowerMockito.when(restTemplate.exchange(eq(innerServicePath + "/inner/scan/commits?repo_id=" + repo_id+"&category="+category), eq(HttpMethod.GET), any(HttpEntity.class),eq(JSONArray.class) )).thenReturn(responseEntity);
        PowerMockito.when(responseEntity.getBody()).thenReturn(commits);
        PowerMockito.when(restTemplate.getForObject(commitServicePath + "/commit-time?commit_id=" + current_commit_id, JSONObject.class)).thenReturn(jsonObject);
        PowerMockito.when(restTemplate.postForObject(eq(tagServicePath), any(List.class), eq(JSONObject.class))).thenReturn(null);

        issueService.startMapping(repo_id,pre_commit_id,current_commit_id,category);

        verify(issueDao,Mockito.atLeastOnce()).getIssueByID(any(String.class));
        verify(rawIssueDao,Mockito.times(2)).batchUpdateIssueId(any(List.class));
        verify(issueDao,Mockito.times(1)).batchUpdateIssue(any(List.class));
        verify(issueDao,Mockito.times(2)).insertIssueList(any(List.class));
        //verify(issueDao,Mockito.times(1)).getSolvedIssueIds(repo_id,pre_commit_id);

    }
    @Test
    @Ignore
    public void updateIssueCount() {

    }
}