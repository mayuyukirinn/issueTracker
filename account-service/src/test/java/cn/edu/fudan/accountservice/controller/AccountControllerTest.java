package cn.edu.fudan.accountservice.controller;

import cn.edu.fudan.accountservice.AccountServiceApplicationTests;
import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.AccountInfo;
import cn.edu.fudan.accountservice.domain.ResponseBean;
import cn.edu.fudan.accountservice.service.AccountService;
import cn.edu.fudan.accountservice.service.impl.AccountServiceImpl;
import cn.edu.fudan.accountservice.tool.TestDataMaker;
import cn.edu.fudan.accountservice.util.MD5Util;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@PrepareForTest({AccountController.class, AccountService.class, AccountServiceImpl.class})
@WebAppConfiguration
public class AccountControllerTest extends AccountServiceApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    AccountService service;

    @Autowired
    @InjectMocks
    AccountController controller;

    private MockMvc mockMvc;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;

    private ResponseBean responseBean ;
    private Account account;
    ObjectMapper mapper;

    @Before
    public void setupMockMvcAndData() throws Exception {
        service = Mockito.mock(AccountService.class);
        mapper = new ObjectMapper();
        MemberModifier.field(AccountController.class, "accountService").set(controller, service);
        //setupMockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        response = new MockHttpServletResponse();

        responseBean = new ResponseBean();
        account = TestDataMaker.accountMaker();
    }

    @Test
    //
    public void loginTestSuccess() throws Exception {
        String username = "admin";
        String password = "admin";

        responseBean.setCode(200);
        responseBean.setData(new AccountInfo(username,  MD5Util.md5(MD5Util.md5(username + password))));
        responseBean.setMsg("登录成功");
        PowerMockito.when(service.login(username,password)).thenReturn(responseBean);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/user/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("username", username)
                .param("password", password)  //.param("password","admins")
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());
    }

    @Test
    //
    public void loginTestFail() throws Exception {
        String username = "admin";
        String password = "admins";

        responseBean.setCode(401);
        responseBean.setMsg("用户名或密码错误");
        PowerMockito.when(service.login(username,password)).thenReturn(responseBean);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/user/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("username", username)
                .param("password", password)  //.param("password","admins")
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(responseBean.getCode(), responseBeanResult.getCode());
    }

    @Test
    public void getAccountID() throws Exception {
        String userToken = "007007";
        PowerMockito.when(service.getAccountByToken(userToken)).thenReturn(account);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/user/accountId")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("userToken", userToken)
                .session(session)
        ).andReturn();

        Assert.assertEquals(account.getUuid(), result.getResponse().getContentAsString());


    }

    @Test
    public void authTestSuccess() throws Exception {
        String token = "token";
        Boolean auth = true;
        PowerMockito.when(service.authByToken(token)).thenReturn(auth);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/auth/"+token)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(200, responseBeanResult.getCode());

    }

    @Test
    public void authTestFail() throws Exception {
        String token = "token";
        Boolean auth = false;
        PowerMockito.when(service.authByToken(token)).thenReturn(auth);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/auth/"+token)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(401, responseBeanResult.getCode());

    }

    @Test
    public void getAccountIds() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        PowerMockito.when(service.getAllAccountId()).thenReturn(list);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/accountIds")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .session(session)
        ).andReturn();

        List listResult = JSONObject.parseObject(result.getResponse().getContentAsString(), List.class);
        Assert.assertEquals(list.size(), listResult.size());
        for(int i=0;i<listResult.size();i++){
            Assert.assertEquals(list.indexOf(i), listResult.indexOf(i));
        }
    }


    @Test
    public void checkUserNameTestExit() throws Exception {
        String accountName = "admin";
        PowerMockito.when(service.isAccountNameExist(accountName)).thenReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/account-name/check")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("accountName",accountName)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(true, responseBeanResult.getData());
    }

    @Test
    public void checkUserNameTestNotExit() throws Exception {
        String accountName = "admin";
        PowerMockito.when(service.isAccountNameExist(accountName)).thenReturn(false);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/account-name/check")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("accountName",accountName)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(false, responseBeanResult.getData());
    }

    @Test
    public void checkEmailTestExit() throws Exception {
        String email = "123@qq.com";
        PowerMockito.when(service.isEmailExist(email)).thenReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/email/check")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("email",email)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(true, responseBeanResult.getData());
    }

    @Test
    public void checkEmailTestNotExit() throws Exception {
        String email = "123@qq.com";
        PowerMockito.when(service.isEmailExist(email)).thenReturn(false);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/email/check")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("email",email)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(false, responseBeanResult.getData());
    }

    @Test
    public void checkNickNameTestExit() throws Exception {
        String nickName = "acc";
        PowerMockito.when(service.isNameExist(nickName)).thenReturn(true);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/nick-name/check")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("nickName",nickName)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(true, responseBeanResult.getData());
    }

    @Test
    public void checkNickNameTestNotExit() throws Exception {
        String nickName = "acc";
        PowerMockito.when(service.isNameExist(nickName)).thenReturn(false);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/user/nick-name/check")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .param("nickName",nickName)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(false, responseBeanResult.getData());
    }

    @Test
    public void createUserTestSuccess() throws Exception {
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        java.lang.String requestJson = ow.writeValueAsString(account);
        doNothing().when(service).addAccount( any(Account.class));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(
                "/user/register")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(200, responseBeanResult.getCode());
    }

    @Test
    public void createUserTestFail() throws Exception {
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        java.lang.String requestJson = ow.writeValueAsString(account);
        PowerMockito.when(service, "addAccount", any(Account.class)).thenThrow(new RuntimeException());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(
                "/user/register")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .session(session)
        ).andReturn();
        ResponseBean responseBeanResult = JSONObject.parseObject(result.getResponse().getContentAsString(), ResponseBean.class);
        Assert.assertEquals(401, responseBeanResult.getCode());
    }
}