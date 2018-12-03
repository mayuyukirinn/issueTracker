package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.IssueServiceApplicationTests;
import cn.edu.fudan.issueservice.domain.Location;
import cn.edu.fudan.issueservice.domain.RawIssue;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.interceptor.AuthTokenInterceptor;
import cn.edu.fudan.issueservice.service.RawIssueService;
import cn.edu.fudan.issueservice.service.impl.RawIssueServiceImpl;
import cn.edu.fudan.issueservice.util.TestDataMaker;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Assert;
import org.junit.Before;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

@PrepareForTest({RawIssueController.class, RawIssueService.class, RawIssueServiceImpl.class, AuthTokenInterceptor.class})
@WebAppConfiguration
public class RawIssueControllerTest extends IssueServiceApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    RawIssueService service;

    @Autowired
    @InjectMocks
    RawIssueController rawIssueController;

    private MockMvc mockMvc;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;

    ObjectMapper mapper;
    RawIssue rawIssue1;
    RawIssue rawIssue2;
    List<RawIssue> list;
    TestDataMaker testDataMaker;
    String token ;

    @Before
    public void setupMockMvc() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        service = Mockito.mock(RawIssueService.class);
        MemberModifier.field(RawIssueController.class, "rawIssueService").set(rawIssueController, service);
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        response = new MockHttpServletResponse();

        mapper = new ObjectMapper();
        testDataMaker = new TestDataMaker();
        token = "token";

        rawIssue1 = testDataMaker.rawIssueMaker1();
        rawIssue2 = testDataMaker.rawIssueMaker2();

        list = new ArrayList<RawIssue>();
        list.add(rawIssue1);
        list.add(rawIssue2);

    }

    @Test
    public void getRawIssueList() throws Exception {
        String issue_id = "iss1";
        PowerMockito.when(service.getRawIssueByIssueId(issue_id)).thenReturn(list);
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/raw-issue")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .param("issue_id", issue_id)
                .session(session)
        ).andReturn();
        List<RawIssue> listResult = JSONObject.parseArray(result.getResponse().getContentAsString(), RawIssue.class);
        Assert.assertEquals(list.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(list.get(i).getUuid(), listResult.get(i).getUuid());
        }
    }

    @Test
    public void getCode() throws Exception {
        Map<String, Object> initialMap = new HashMap<>();
        String codeDetail = "{\"code\":\"XXXXXX\"}";
        JSONObject codeResponse = JSONObject.parseObject(codeDetail);
        initialMap.put("code", codeResponse);
        String project_id = "pro1";
        String commit_id = "cmm1";
        String file_path = "file_path";
        PowerMockito.when(service.getCode(project_id,commit_id,file_path)).thenReturn(initialMap);
        MemberModifier.stub(MemberMatcher.method(AuthTokenInterceptor.class, "preHandle")).toReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/raw-issue/code")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("token", token)
                .param("project_id", project_id)
                .param("commit_id", commit_id)
                .param("file_path", file_path)
                .session(session)
        ).andReturn();
        Map responseMap = JSON.parseObject(result.getResponse().getContentAsString());
        Assert.assertEquals(initialMap.get("code").toString(),responseMap.get("code").toString());
    }

    @Test
    public void addRawIssues() throws Exception {
        /*
            添加成功
         */
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(200);
        responseBean.setMsg("rawIssue add success!");
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        java.lang.String requestJson = ow.writeValueAsString(list);
        doNothing().when(service).insertRawIssueList(list);
        MvcResult resultCorrect = mockMvc.perform(MockMvcRequestBuilders.post("/inner/raw-issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(resultCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());
        /*
            添加失败
         */
        PowerMockito.when(service, "insertRawIssueList", any(List.class)).thenThrow(new RuntimeException());
        responseBean.setCode(401);
        responseBean.setMsg("rawIssue add failed!");
        MvcResult resultIncorrect = mockMvc.perform(MockMvcRequestBuilders.post("/inner/raw-issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andReturn();
        ResponseBean responseBeanResultIncorrect = JSONObject.parseObject(resultIncorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResultIncorrect.getCode());
    }

    @Test
    public void deleteRawIssue() throws Exception {
        /*
            删除成功
         */
        String repo = "repo";
        String category = "bug";
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(200);
        responseBean.setMsg("rawIssue delete success!");
        doNothing().when(service).deleteRawIssueByRepoIdAndCategory(repo,category);
        MvcResult resultCorrect = mockMvc.perform(MockMvcRequestBuilders.delete("/inner/raw-issue/"+category+"/"+repo)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(resultCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());

        /*
            删除失败
         */
        responseBean.setCode(401);
        responseBean.setMsg("rawIssue delete failed!");
        PowerMockito.when(service, "deleteRawIssueByRepoIdAndCategory", eq(repo),eq(category)).thenThrow(new RuntimeException());
        MvcResult resultInCorrect = mockMvc.perform(MockMvcRequestBuilders.delete("/inner/raw-issue/"+category+"/"+repo)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResultIncorrect = JSONObject.parseObject(resultInCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResultIncorrect.getCode());
    }

    @Test
    public void updateRawIssues() throws Exception {
        /*
            issue 更新成功
         */
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(200);
        responseBean.setMsg("rawIssue update success!");
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        java.lang.String requestJson = ow.writeValueAsString(list);
        doNothing().when(service).batchUpdateIssueId(list);
        MvcResult resultCorrect = mockMvc.perform(MockMvcRequestBuilders.put("/inner/raw-issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(resultCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());

        /*
            issue 更新失败
         */
        responseBean.setCode(401);
        responseBean.setMsg("rawIssue update failed!");
        PowerMockito.when(service, "batchUpdateIssueId", any(List.class)).thenThrow(new RuntimeException());
        MvcResult resultInCorrect = mockMvc.perform(MockMvcRequestBuilders.put("/inner/raw-issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andReturn();
        ResponseBean responseBeanResultIncorrect = JSONObject.parseObject(resultInCorrect.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResultIncorrect.getCode());
    }

    /*
        等scan单元测试之后完善
     */
    @Test
    public void getRawIssues() throws Exception {
        String commit_id = "cmm1";
        String category = "category";
        PowerMockito.when(service.getRawIssueByCommitIDAndCategory(commit_id,category)).thenReturn(list);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/inner/raw-issue/list-by-commit")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("commit_id", commit_id)
                .param("category",category)
                .session(session)
        ).andReturn();
        List<RawIssue> listResult = JSONObject.parseArray(result.getResponse().getContentAsString(), RawIssue.class);
        Assert.assertEquals(list.size(), listResult.size());
        for (int i = 0; i < listResult.size(); ++i) {
            Assert.assertEquals(list.get(i).getUuid(), listResult.get(i).getUuid());
        }
    }
}