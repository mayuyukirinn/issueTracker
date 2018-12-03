package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.IssueServiceApplicationTests;
import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.domain.IssueCount;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.interceptor.AuthTokenInterceptor;
import cn.edu.fudan.issueservice.service.IssueService;
import cn.edu.fudan.issueservice.service.impl.IssueServiceImpl;
import cn.edu.fudan.issueservice.util.TestDataMaker;
import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

@PrepareForTest({IssueController.class, IssueService.class, IssueServiceImpl.class, AuthTokenInterceptor.class})
@WebAppConfiguration
public class IssueControllerTest extends IssueServiceApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    IssueService service;

    @Autowired
    @InjectMocks
    IssueController issueController;

    private MockMvc mockMvc;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;
    Map<String, Object> map;
    ObjectMapper mapper;

    List<Issue> list;
    Issue issue1;
    Issue issue2;
    TestDataMaker testDataMaker;
    String token ;

    @Before
    public void setupMockMvc() throws Exception {
        testDataMaker = new TestDataMaker();
        service = Mockito.mock(IssueService.class);
        MemberModifier.field(IssueController.class, "issueService").set(issueController, service);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        response = new MockHttpServletResponse();

        JSONArray tag_ids = new JSONArray();
        tag_ids.add("tag1");
        tag_ids.add("tag2");
        JSONArray types = new JSONArray();
        types.add("dl");
        types.add("ball");
        map = new LinkedHashMap<String, Object>();
        map.put("project_id", "222");
        map.put("size", "2");
        map.put("page", "0");
        map.put("category","category");
        map.put("tags",tag_ids);
        map.put("types",types);


        mapper = new ObjectMapper();

        issue1 = testDataMaker.issueMaker1();
        issue2 = testDataMaker.issueMaker2();

        list = new ArrayList<>();
        list.add(issue1);
        list.add(issue2);

        token = "token";
    }

    @Test
    public void getIssues() throws Exception {
        String project_id = "pro1";
        String page = "0";
        String size = "2";
        String category = "category";
        PowerMockito.when(service.getIssueList(project_id, 0,2,category)).thenReturn(list);
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/issue")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .param("project_id", project_id)
                .param("page",page)
                .param("size",size)
                .param("category",category)
                .session(session)
        ).andReturn();
        List<Issue> listResult = JSONObject.parseArray(result.getResponse().getContentAsString(), Issue.class);
        Assert.assertEquals(list.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(list.get(i).getUuid(), listResult.get(i).getUuid());
        }
    }

    /*
     *   需要启动account-service
     */
    @Test
    public void getDashBoardInfo() throws Exception {
        String duration = "30";
        String project_id = "pro1";
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("newIssueCount", 1);
        resultMap.put("eliminatedIssueCount", 2);
        resultMap.put("remainingIssueCount", 3);
        PowerMockito.when(service.getDashBoardInfo(duration,project_id,token,"bug")).thenReturn(resultMap);
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/issue/dashboard")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .param("duration",duration)
                .param("project_id",project_id)
                .session(session)
        ).andReturn();
        Map responseMap = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(resultMap.get("newIssueCount"), responseMap.get("newIssueCount"));
        Assert.assertEquals(resultMap.get("eliminatedIssueCount"), responseMap.get("eliminatedIssueCount"));
        Assert.assertEquals(resultMap.get("remainingIssueCount"), responseMap.get("remainingIssueCount"));

    }

    @Test
    public void addIssues() throws Exception {
        /*
            添加成功
         */
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(200);
        responseBean.setMsg("issues add success!");
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        java.lang.String requestJson = ow.writeValueAsString(list);
        doNothing().when(service).insertIssueList(list);
        MvcResult resultCorrect = mockMvc.perform(MockMvcRequestBuilders.post("/inner/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(resultCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());
        /*
            添加失败
         */
        PowerMockito.when(service, "insertIssueList", any(List.class)).thenThrow(new RuntimeException());
        responseBean.setCode(401);
        responseBean.setMsg("issues add failed!");
        MvcResult resultIncorrect = mockMvc.perform(MockMvcRequestBuilders.post("/inner/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andReturn();
        ResponseBean responseBeanResultIncorrect = JSONObject.parseObject(resultIncorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResultIncorrect.getCode());

    }

    @Test
    public void deleteIssues() throws Exception {
        /*
            删除成功
         */
        String repo = "repo";
        String category = "bug";
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(200);
        responseBean.setMsg("issues delete success!");
        doNothing().when(service).deleteIssueByRepoIdAndCategory(repo,"bug");
        MvcResult resultCorrect = mockMvc.perform(MockMvcRequestBuilders.delete("/inner/issue/"+category+"/"+repo)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(resultCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());

        /*
            删除失败
         */
        responseBean.setCode(401);
        responseBean.setMsg("issues delete failed!");
        PowerMockito.when(service, "deleteIssueByRepoIdAndCategory", eq(repo),eq(category)).thenThrow(new RuntimeException());
        MvcResult resultInCorrect = mockMvc.perform(MockMvcRequestBuilders.delete("/inner/issue/"+category+"/"+repo)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResultIncorrect = JSONObject.parseObject(resultInCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResultIncorrect.getCode());

    }

    @Test
    public void updateIssues() throws Exception {
        /*
            issue 更新成功
         */
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(200);
        responseBean.setMsg("issues update success!");
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        java.lang.String requestJson = ow.writeValueAsString(list);
        doNothing().when(service).batchUpdateIssue(list);
        MvcResult resultCorrect = mockMvc.perform(MockMvcRequestBuilders.put("/inner/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(resultCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());

        /*
            issue 更新失败
         */
        responseBean.setCode(401);
        responseBean.setMsg("issues update failed!");
        PowerMockito.when(service, "batchUpdateIssue", any(List.class)).thenThrow(new RuntimeException());
        MvcResult resultInCorrect = mockMvc.perform(MockMvcRequestBuilders.put("/inner/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andReturn();
        ResponseBean responseBeanResultIncorrect = JSONObject.parseObject(resultInCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResultIncorrect.getCode());
    }

    /*
        等scan单元测试之后补充
     */
    @Test
    public void mapping() throws Exception {
        /*
            mapping 成功
         */

        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(200);
        responseBean.setMsg("issues mapping success!");
        String repo_id = "repoid";
        String pre_commit_id = "preCid";
        String current_commit_id = "currCid";
        String category = "category";

        Map<String, Object> initialMap = new HashMap<>();
        initialMap.put("repo_id", repo_id);
        initialMap.put("pre_commit_id",pre_commit_id);
        initialMap.put("current_commit_id", current_commit_id);
        initialMap.put("category",category);

        JSONObject requestParam = JSONObject.parseObject(JSON.toJSONString(initialMap));
        doNothing().when(service).startMapping(repo_id,pre_commit_id,current_commit_id,category);
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult resultCorrect = mockMvc.perform(MockMvcRequestBuilders.post("/inner/issue/mapping")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestParam.toJSONString())
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(resultCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());

        /*
            mapping 失败
         */

        responseBean.setCode(401);
        responseBean.setMsg("issues mapping failed!");
        PowerMockito.when(service, "startMapping", eq(repo_id),eq(pre_commit_id),eq(current_commit_id),eq(category)).thenThrow(new RuntimeException());
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult resultInCorrect = mockMvc.perform(MockMvcRequestBuilders.post("/inner/issue/mapping")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestParam.toJSONString())
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResultIncorrect  = JSONObject.parseObject(resultInCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResultIncorrect .getCode());
    }

    @Test
    public void getExistIssueTypes() throws Exception {
        List<String> listString = new ArrayList<String>();
        listString.add("dl");
        listString.add("ball");
        PowerMockito.when(service.getExistIssueTypes("bug")).thenReturn(listString);
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/issue/issue-types")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .session(session)
        ).andReturn();
        List<String> listResult = JSONObject.parseArray(result.getResponse().getContentAsString(), String.class);
        Assert.assertEquals(listString.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(listString.get(i),listResult.get(i));
        }
    }

    @Test
    public void filterIssues() throws Exception {
        JSONObject requestParam = JSONObject.parseObject(JSON.toJSONString(map));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalPage", 1);
        resultMap.put("totalCount", 2);
        resultMap.put("issueList", list);
        PowerMockito.when(service.getFilteredIssueList(requestParam)).thenReturn(resultMap);
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/issue/filter")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .content(requestParam.toJSONString())
                .session(session)
        ).andReturn();
        Map responseMap = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(resultMap.get("totalPage"),responseMap.get("totalPage"));
        Assert.assertEquals(resultMap.get("totalCount"),responseMap.get("totalCount"));
        List<Issue> listResultIssues = JSONObject.parseArray(responseMap.get("issueList").toString(),Issue.class);
        Assert.assertEquals(list.size(), listResultIssues.size());
        for (int i = 0; i < listResultIssues.size(); ++i) {
            Assert.assertEquals(list.get(i).getUuid(), listResultIssues.get(i).getUuid());
        }
    }

    @Test
    public void getStatisticalResults() throws Exception {
        int month = 1;
        String project_id = "0";
        Map<String, Object> resultMap = new HashMap<>();
        List<IssueCount> listIssueCount = new ArrayList<>();
        listIssueCount.add(new IssueCount(1, 2, 3));
        listIssueCount.add(new IssueCount(2, 3, 4));
        resultMap.put("data",listIssueCount);
        PowerMockito.when(service.getStatisticalResults(month, project_id,token,"bug")).thenReturn(resultMap);
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/issue/statistical-results")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .param("month", String.valueOf(month))
                .param("project_id",project_id)
                .session(session)
        ).andReturn();
        Map responseMap = JSON.parseObject(result.getResponse().getContentAsString());
        List<IssueCount> listRresultIssueCount = JSONObject.parseArray(responseMap.get("data").toString(),IssueCount.class);
        Assert.assertEquals(listIssueCount.size(), listRresultIssueCount.size());
        for (int i = 0; i < listRresultIssueCount.size(); ++i) {
            Assert.assertEquals(listIssueCount.get(i).toString(), listRresultIssueCount.get(i).toString());
        }
    }
}