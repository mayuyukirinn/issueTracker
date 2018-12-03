package cn.edu.fudan.scanservice.service;

import cn.edu.fudan.scanservice.ScanServiceApplicationTests;
import cn.edu.fudan.scanservice.dao.ScanDao;
import cn.edu.fudan.scanservice.domain.Scan;
import cn.edu.fudan.scanservice.service.impl.ScanServiceImpl;
import cn.edu.fudan.scanservice.util.TestDataMaker;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@PrepareForTest({ScanService.class, ScanServiceImpl.class, ScanDao.class, RestTemplate.class, ResponseEntity.class})
public class ScanServiceTest extends ScanServiceApplicationTests {
    @Value("${inner.service.path}")
    private String innerServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;

    @Mock
    private ScanDao scanDao;

    @Mock
    private ResponseEntity responseEntity;


    @Mock
    private RestTemplate restTemplate;

    @Autowired
    @InjectMocks
    ScanService scanService = new ScanServiceImpl();

    private Scan scan;
    private Scan scan2;
    private TestDataMaker testDataMaker;
    private List<Scan> scanList;

    @Before
    public void setup() throws Exception {
        MemberModifier.field(ScanServiceImpl.class, "scanDao").set(scanService, scanDao);
        MemberModifier.field(ScanServiceImpl.class, "restTemplate").set(scanService, restTemplate);

        testDataMaker = new TestDataMaker();
        scan = testDataMaker.scanMakerSc1();
        scan2 = testDataMaker.scanMakerSc2();
        scanList = new ArrayList<>();
        scanList.add(scan);
        scanList.add(scan2);
    }

    @Test
    public void insertOneScan() {
        doNothing().when(scanDao).insertOneScan(scan);
        scanService.insertOneScan(scan);
        Mockito.verify(scanDao,Mockito.times(1)).insertOneScan(scan);
    }

    @Test
    public void deleteScanByRepoId() {
        String repoId = "repo1";
        doNothing().when(scanDao).deleteScanByRepoIdAndCategory(repoId,"bug");
        scanService.deleteScanByRepoIdAndCategory(repoId,"bug");
        verify(scanDao,times(1)).deleteScanByRepoIdAndCategory(repoId,"bug");
    }

    @Test
    public void updateOneScan() {
        doNothing().when(scanDao).updateOneScan(scan);
        scanService.updateOneScan(scan);
        verify(scanDao,times(1)).updateOneScan(scan);
    }

    /*
        访问commit-service返回的commit根据时间排序从最新起排
     */
    @Test
    public void getCommits() {
        String msg ="";
        String project_id = "pro1";
        Integer page = 1;
        Integer size = 1;
        String category = "category";
        String repo_id = "repoId";
        Mockito.when(restTemplate.exchange(eq(innerServicePath + "/inner/project/repo-id?project-id=" + project_id), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);
        Mockito.when(responseEntity.getBody()).thenReturn(repo_id);

        List<String> scannedCommitId = new ArrayList<>();
        scannedCommitId.add("comm0");
        scannedCommitId.add("comm1");

        JSONObject commit2 = new JSONObject();
        commit2.put("commit_id","comm2");
        JSONObject commit1 = new JSONObject();
        commit1.put("commit_id","comm1");
        JSONArray data = new JSONArray();
        data.add(commit2);
        data.add(commit1);
        JSONObject commitResponse  = new JSONObject();
        commitResponse.put("data",data);
        Map result;
        /*
            当请求的commit响应为空
         */
        Mockito.when(restTemplate.getForObject(commitServicePath + "?repo_id=" + repo_id + "&page=" + page + "&per_page=" + size + "&is_whole=true", JSONObject.class)).thenReturn(null);
        try{
            result = (Map)scanService.getCommits(project_id,page,size,true,category);
        }catch(RuntimeException e){
            msg = e.getMessage();
        }
        Assert.assertEquals("commit query failed!",msg);

        /*
            当请求的commit响应不为空，且相应的repo和category未sacn过，且展示全部commit,且结果数量大于一页数量
         */
        size = 1;
        page = 1;
        Mockito.when(restTemplate.getForObject(commitServicePath + "?repo_id=" + repo_id + "&page=" + page + "&per_page=" + size + "&is_whole=true", JSONObject.class)).thenReturn(commitResponse);
        PowerMockito.when(scanDao.getScannedCommits(repo_id,category)).thenReturn(Collections.emptyList());
        result = (Map)scanService.getCommits(project_id,page,size,true,category);
        Assert.assertEquals(2,result.get("totalCount"));
        JSONArray resultCommitArray = JSONArray.parseArray(result.get("commitList").toString());
        Assert.assertEquals(1,resultCommitArray.size());
        JSONObject resultCommit = (JSONObject)(resultCommitArray.get(0));
        Assert.assertEquals(false,resultCommit.get("isScanned"));

        /*
            当请求的commit响应不为空，且相应的repo和category未sacn过，且展示全部commit,且结果数量不大于一页数量
         */
        size = 3;
        page = 1;
        Mockito.when(restTemplate.getForObject(commitServicePath + "?repo_id=" + repo_id + "&page=" + page + "&per_page=" + size + "&is_whole=true", JSONObject.class)).thenReturn(commitResponse);
        PowerMockito.when(scanDao.getScannedCommits(repo_id,category)).thenReturn(Collections.emptyList());
        result = (Map)scanService.getCommits(project_id,page,size,true,category);
        Assert.assertEquals(2,result.get("totalCount"));
        resultCommitArray = JSONArray.parseArray(result.get("commitList").toString());
        Assert.assertEquals(2,resultCommitArray.size());
        for(int i = 0; i<resultCommitArray.size();++i){
            resultCommit = (JSONObject)(resultCommitArray.get(i));
            Assert.assertEquals(false,resultCommit.get("isScanned"));
        }

        /*
            当请求的commit响应不为空，且相应的repo和category未sacn过，且不展示全部commit,且结果数量不大于一页数量
         */
        size = 3;
        page = 1;
        Mockito.when(restTemplate.getForObject(commitServicePath + "?repo_id=" + repo_id + "&page=" + page + "&per_page=" + size + "&is_whole=true", JSONObject.class)).thenReturn(commitResponse);
        PowerMockito.when(scanDao.getScannedCommits(repo_id,category)).thenReturn(Collections.emptyList());
        result = (Map)scanService.getCommits(project_id,page,size,false,category);
        Assert.assertEquals(2,result.get("totalCount"));
        resultCommitArray = JSONArray.parseArray(result.get("commitList").toString());
        Assert.assertEquals(2,resultCommitArray.size());
        for(int i = 0; i<resultCommitArray.size();++i){
            resultCommit = (JSONObject)(resultCommitArray.get(i));
            Assert.assertEquals(false,resultCommit.get("isScanned"));
        }

        /*
            当请求的commit响应不为空，且相应的repo和category已sacn过，且展示全部commit,且结果数量不大于一页数量
         */
        size = 3;
        page = 1;
        Mockito.when(restTemplate.getForObject(commitServicePath + "?repo_id=" + repo_id + "&page=" + page + "&per_page=" + size + "&is_whole=true", JSONObject.class)).thenReturn(commitResponse);
        PowerMockito.when(scanDao.getScannedCommits(repo_id,category)).thenReturn(scannedCommitId);
        result = (Map)scanService.getCommits(project_id,page,size,true,category);
        Assert.assertEquals(2,result.get("totalCount"));
        resultCommitArray = JSONArray.parseArray(result.get("commitList").toString());
        JSONObject resultCommit1 = (JSONObject)(resultCommitArray.get(0));
        Assert.assertEquals(false,resultCommit1.get("isScanned"));
        JSONObject resultCommit2 = (JSONObject)(resultCommitArray.get(1));
        Assert.assertEquals(true,resultCommit2.get("isScanned"));

        /*
            当请求的commit响应不为空，且相应的repo和category已sacn过，且不展示全部commit,且结果数量不大于一页数量
         */
        size = 3;
        page = 1;
        Mockito.when(restTemplate.getForObject(commitServicePath + "?repo_id=" + repo_id + "&page=" + page + "&per_page=" + size + "&is_whole=true", JSONObject.class)).thenReturn(commitResponse);
        PowerMockito.when(scanDao.getScannedCommits(repo_id,category)).thenReturn(scannedCommitId);
        result = (Map)scanService.getCommits(project_id,page,size,false,category);
        Assert.assertEquals(1,result.get("totalCount"));
        resultCommitArray = JSONArray.parseArray(result.get("commitList").toString());
        resultCommit = (JSONObject)(resultCommitArray.get(0));
        Assert.assertEquals(false,resultCommit.get("isScanned"));
        Assert.assertEquals("comm2",resultCommit.get("commit_id"));


    }

    @Test
    public void getScannedCommits() {
        String repoId = "repo1";
        String category = "category";
        Mockito.when(scanDao.getScans(repoId,category)).thenReturn(scanList);
        List<Scan> listResult = (List<Scan>) scanService.getScannedCommits(repoId,category);
        Assert.assertEquals(scanList.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(scanList.get(i).getUuid(),listResult.get(i).getUuid());
        }
    }

    @Test
    public void getLatestScannedCommitId() {
        String repoId = "repo1";
        String category = "category";
        String commitId = "comm1";
        Mockito.when(scanDao.getLatestScannedCommitId(repoId,category)).thenReturn(commitId);
        String result = scanService.getLatestScannedCommitId(repoId,category);
        Assert.assertEquals(commitId,result);
    }
}