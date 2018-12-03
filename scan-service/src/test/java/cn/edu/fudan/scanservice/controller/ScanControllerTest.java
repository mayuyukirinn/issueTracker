package cn.edu.fudan.scanservice.controller;

import cn.edu.fudan.scanservice.ScanServiceApplicationTests;
import cn.edu.fudan.scanservice.domain.ResponseBean;
import cn.edu.fudan.scanservice.domain.Scan;
import cn.edu.fudan.scanservice.interceptor.AuthTokenInterceptor;
import cn.edu.fudan.scanservice.service.KafkaService;
import cn.edu.fudan.scanservice.service.ScanService;
import cn.edu.fudan.scanservice.service.impl.ScanServiceImpl;
import cn.edu.fudan.scanservice.util.TestDataMaker;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@PrepareForTest({ScanController.class, ScanService.class, ScanServiceImpl.class, KafkaService.class,AuthTokenInterceptor.class})
@WebAppConfiguration
public class ScanControllerTest extends ScanServiceApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ScanService scanService;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    @InjectMocks
    private ScanController scanController;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;
    private MockMvc mockMvc;
    private TestDataMaker testDataMaker;
    ObjectMapper mapper;
    Scan scan1;

    String token;

    @Before
    public void setupMockMvc() throws Exception {
        scanService = Mockito.mock(ScanService.class);
        kafkaService = Mockito.mock(KafkaService.class);
        mapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        response = new MockHttpServletResponse();

        testDataMaker = new TestDataMaker();
        scan1 = testDataMaker.scanMakerSc1();

        MemberModifier.field(ScanController.class, "scanService").set(scanController, scanService);
        MemberModifier.field(ScanController.class, "kafkaService").set(scanController, kafkaService);

        token = "password";
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
    }

    @Test
    public void scan() throws Exception{
        JSONObject requestParam = new JSONObject();


        /*
            当访问成功时
         */
        doNothing().when(kafkaService).scanByRequest(any(JSONObject.class));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/scan")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestParam.toJSONString())
                .session(session)
        ).andReturn();
        Map map = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(200, map.get("code"));
        /*
            当访问失败时
         */
        doThrow(new RuntimeException()).when(kafkaService).scanByRequest(any(JSONObject.class));
        result = mockMvc.perform(MockMvcRequestBuilders.post("/scan")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestParam.toJSONString())
                .session(session)
        ).andReturn();
        map = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(401, map.get("code"));
    }

    @Test
    public void getCommits() throws Exception{
        String project_id = "project_id";
        Integer page = 1;
        Integer size = 10;
        Boolean is_whole = false;
        String category = "category";
        Mockito.when(scanService.getCommits(project_id,page,size,is_whole,category)).thenReturn(null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/scan/commits")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .param("project_id",project_id)
                .param("page",page.toString())
                .param("size",size.toString())
                .param("is_whole",is_whole.toString())
                .param("category",category)
                .session(session)
        ).andReturn();

        verify(scanService,times(1)).getCommits(project_id,page,size,is_whole,category);
    }

    @Test
    public void deleteScans() throws Exception{
        /*
            当删除成功时
         */
        String repoId = "repoId";
        String category = "bug";
        doNothing().when(scanService).deleteScanByRepoIdAndCategory(repoId,category);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/inner/scan/"+category+"/"+repoId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .session(session)
        ).andReturn();
        Map map = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(200, map.get("code"));
        /*
            当删除失败时
         */
        doThrow(new RuntimeException()).when(scanService).deleteScanByRepoIdAndCategory(repoId,category);
        result  = mockMvc.perform(MockMvcRequestBuilders.delete("/inner/scan/"+category+"/"+repoId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .session(session)
        ).andReturn();
        map = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(401, map.get("code"));

    }

    @Test
    public void updateScan() throws Exception{
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        java.lang.String requestJson = ow.writeValueAsString(scan1);

        /*
            当更新成功时
         */
        doNothing().when(scanService).updateOneScan(any(Scan.class));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/inner/scan")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestJson)
                .session(session)
        ).andReturn();
        Map map = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(200, map.get("code"));
        /*
            当更新失败时
         */
        doThrow(new RuntimeException()).when(scanService).updateOneScan(any(Scan.class));
        result  = mockMvc.perform(MockMvcRequestBuilders.put("/inner/scan")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestJson)
                .session(session)
        ).andReturn();
        map = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(401, map.get("code"));
    }

    @Test
    public void addOneScan() throws Exception{
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        java.lang.String requestJson = ow.writeValueAsString(scan1);

        /*
            当添加成功时
         */
        doNothing().when(scanService).insertOneScan(any(Scan.class));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/inner/scan")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestJson)
                .session(session)
        ).andReturn();
        Map map = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(200, map.get("code"));
        /*
            当添加失败时
         */
        doThrow(new RuntimeException()).when(scanService).insertOneScan(any(Scan.class));
        result  = mockMvc.perform(MockMvcRequestBuilders.post("/inner/scan")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestJson)
                .session(session)
        ).andReturn();
        map = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(401, map.get("code"));

    }

    @Test
    public void getScannedCommits() throws Exception{
        String repo_id = "repo_id";
        String category = "category";
        Mockito.when(scanService.getScannedCommits(repo_id,category)).thenReturn(null);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/inner/scan/commits")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .param("repo_id",repo_id)
                .param("category",category)
                .session(session)
        ).andReturn();
        verify(scanService,times(1)).getScannedCommits(repo_id,category);
    }

    @Test
    public void getLatestScannedCommitId() throws Exception{
        String repo_id = "repo_id";
        String category = "category";
        Mockito.when(scanService.getLatestScannedCommitId(repo_id,category)).thenReturn("ball");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/inner/scan/last-commit")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .param("repo_id",repo_id)
                .param("category",category)
                .session(session)
        ).andReturn();
        verify(scanService,times(1)).getLatestScannedCommitId(repo_id,category);
    }
}