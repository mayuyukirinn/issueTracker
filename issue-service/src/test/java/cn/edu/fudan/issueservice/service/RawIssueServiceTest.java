package cn.edu.fudan.issueservice.service;


import cn.edu.fudan.issueservice.IssueServiceApplicationTests;
import cn.edu.fudan.issueservice.dao.LocationDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.domain.Location;
import cn.edu.fudan.issueservice.domain.RawIssue;
import cn.edu.fudan.issueservice.service.impl.RawIssueServiceImpl;
import cn.edu.fudan.issueservice.util.TestDataMaker;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PrepareForTest({RawIssueService.class, RawIssueServiceImpl.class, RawIssueDao.class, LocationDao.class, RestTemplate.class, ResponseEntity.class})
public class RawIssueServiceTest extends IssueServiceApplicationTests {

    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${code.service.path}")
    private String codeServicePath;
    @Value("${repoHome}")
    private String repoHome;

    @Value("${inner.service.path}")
    private String innerServicePath;

    @Mock
    private RawIssueDao rawIssueDao;
    @Mock
    private LocationDao locationDao;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ResponseEntity repoIdResponseEntity;

    @Autowired
    @InjectMocks
    private RawIssueService rawIssueService = new RawIssueServiceImpl();

    List<RawIssue> list;
    RawIssue rawIssue1;
    RawIssue rawIssue2;
    RawIssue rawIssue3;

    Location location1;

    private TestDataMaker testDataMaker;

    @Before
    public void setup() throws Exception {

        testDataMaker = new TestDataMaker();
        list = new ArrayList<>();
        rawIssue1 = testDataMaker.rawIssueMaker1();
        rawIssue2 = testDataMaker.rawIssueMaker2();
        rawIssue3 = testDataMaker.rawIssueMaker3();
        list.add(rawIssue1);
        list.add(rawIssue2);

        location1 = testDataMaker.locationMaker1();

        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(rawIssueService,"rawIssueDao",rawIssueDao);
        ReflectionTestUtils.setField(rawIssueService,"locationDao",locationDao);
        ReflectionTestUtils.setField(rawIssueService,"restTemplate",restTemplate);

        System.out.println("finish mocking");

    }

    @Test
    public void insertRawIssueList() {
        doNothing().when(rawIssueDao).insertRawIssueList(list);
        doNothing().when(locationDao).insertLocationList(any(List.class));
        rawIssueService.insertRawIssueList(list);
        verify(rawIssueDao,times(1)).insertRawIssueList(list);
        verify(locationDao,times(1)).insertLocationList(any(List.class));
    }

    @Test
    public void deleteRawIssueByRepoId() {
        String repoId = "repo_id";
        doNothing().when(rawIssueDao).deleteRawIssueByRepoIdAndCategory(repoId,"bug");
        doNothing().when(locationDao).deleteLocationByRepoIdAndCategory(repoId,"bug");
        rawIssueService.deleteRawIssueByRepoIdAndCategory(repoId,"bug");
        verify(rawIssueDao,times(1)).deleteRawIssueByRepoIdAndCategory(repoId,"bug");
        verify(locationDao,times(1)).deleteLocationByRepoIdAndCategory(repoId,"bug");
    }

    @Test
    public void batchUpdateIssueId() {
        doNothing().when(rawIssueDao).batchUpdateIssueId(list);
        rawIssueService.batchUpdateIssueId(list);
        verify(rawIssueDao,times(1)).batchUpdateIssueId(list);
    }

    @Test
    public void getRawIssueByCommitIDAndCategory() {
        List<RawIssue> listByCommitIDAndCategory = new ArrayList<>();
        listByCommitIDAndCategory.add(rawIssue2);
        listByCommitIDAndCategory.add(rawIssue3);
        String commit_id = "comm2";
        String category = "category";
        Mockito.when(rawIssueDao.getRawIssueByCommitIDAndCategory(commit_id,category)).thenReturn(listByCommitIDAndCategory);
        List<RawIssue> listResult = rawIssueService.getRawIssueByCommitIDAndCategory(commit_id,category);
        Assert.assertEquals(listByCommitIDAndCategory.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(listByCommitIDAndCategory.get(i),listResult.get(i));
        }
    }

    @Test
    public void getRawIssueByIssueId() {
        List<RawIssue> listByIssueId = new ArrayList<>();
        listByIssueId.add(rawIssue2);
        listByIssueId.add(rawIssue3);
        String issueId = "iss2";
        Mockito.when(rawIssueDao.getRawIssueByIssueId(issueId)).thenReturn(listByIssueId);
        List<RawIssue> listResult = rawIssueService.getRawIssueByIssueId(issueId);
        Assert.assertEquals(listByIssueId.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(listByIssueId.get(i),listResult.get(i));
        }
    }

    @Test
    public void getCode() {
        String project_id = "pro1";
        String commit_id = "comm1";
        String file_path = "DatabaseTool.java";
        String repo_id = "repo_id";
        String status = "Successful";
        String content = "content";

        JSONObject response = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("status",status);
        response.put("data",data);

        JSONObject codeResponse = new JSONObject();
        JSONObject codeData = new JSONObject();
        codeData.put("status",status);
        codeData.put("content",content);
        codeResponse.put("data",codeData);

        PowerMockito.when(restTemplate.exchange(eq(innerServicePath + "/inner/project/repo-id?project-id=" + project_id), eq(HttpMethod.GET), any(HttpEntity.class),eq(String.class) )).thenReturn(repoIdResponseEntity);
        PowerMockito.when(repoIdResponseEntity.getBody()).thenReturn(repo_id);
        PowerMockito.when(restTemplate.getForObject(commitServicePath + "/checkout?repo_id=" + repo_id + "&commit_id=" + commit_id, JSONObject.class)).thenReturn(response);
        PowerMockito.when(restTemplate.getForObject(codeServicePath + "?file_path=" + repoHome + file_path, JSONObject.class)).thenReturn(codeResponse);

        /*
            当数据返回全部成功时
         */
        Map result = (Map) rawIssueService.getCode(project_id,commit_id,file_path);
        Assert.assertEquals(content,result.get("code"));

        /*
            当checkout 失败时
         */
        PowerMockito.when(restTemplate.getForObject(commitServicePath + "/checkout?repo_id=" + repo_id + "&commit_id=" + commit_id, JSONObject.class)).thenReturn(null);
        try{
            result = (Map) rawIssueService.getCode(project_id,commit_id,file_path);
        }catch(RuntimeException e){
            Assert.assertEquals("check out failed!",e.getMessage());
        }

        /*
            当获取code 失败时
         */
        PowerMockito.when(restTemplate.getForObject(commitServicePath + "/checkout?repo_id=" + repo_id + "&commit_id=" + commit_id, JSONObject.class)).thenReturn(response);
        PowerMockito.when(restTemplate.getForObject(codeServicePath + "?file_path=" + repoHome + file_path, JSONObject.class)).thenReturn(codeResponse);
        try{
            result = (Map) rawIssueService.getCode(project_id,commit_id,file_path);
        }catch(RuntimeException e){
            Assert.assertEquals("load file failed!",e.getMessage());
        }
    }

    @Test
    public void getLocationsByRawIssueId() {
        String raw_issue_id = "raw1";
        List<Location> locations = new ArrayList<>();
        locations.add(location1);
        Mockito.when(locationDao.getLocations(raw_issue_id)).thenReturn(locations);
        List<Location> listResult = rawIssueService.getLocationsByRawIssueId(raw_issue_id);
        Assert.assertEquals(locations.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(locations.get(i),listResult.get(i));
        }
    }

}