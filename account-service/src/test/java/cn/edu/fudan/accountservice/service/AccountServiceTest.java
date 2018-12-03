package cn.edu.fudan.accountservice.service;

import cn.edu.fudan.accountservice.AccountServiceApplicationTests;
import cn.edu.fudan.accountservice.dao.AccountDao;
import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.AccountInfo;
import cn.edu.fudan.accountservice.domain.ResponseBean;
import cn.edu.fudan.accountservice.service.impl.AccountServiceImpl;
import cn.edu.fudan.accountservice.tool.TestDataMaker;
import cn.edu.fudan.accountservice.util.MD5Util;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@PrepareForTest({AccountService.class, AccountServiceImpl.class, AccountDao.class,StringRedisTemplate.class,ValueOperations.class})
public class AccountServiceTest extends AccountServiceApplicationTests {

    @Mock
    private AccountDao accountDao;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations valueOperations;

    @Autowired
    @InjectMocks
    private AccountService accountService = new AccountServiceImpl();

    private ResponseBean responseBean;
    private Account account;


    @Before
    public void setupData() throws Exception {
        responseBean = new ResponseBean();
        account = TestDataMaker.accountMaker();
        MemberModifier.field(AccountServiceImpl.class, "accountDao").set(accountService, accountDao);
        MemberModifier.field(AccountServiceImpl.class, "stringRedisTemplate").set(accountService, stringRedisTemplate);
        System.out.println("finish mocking");
    }


    @Test
    public void login() {
        String token = "token";
        /*
            正确的用户名及密码
         */
        String username = "admin";
        String password = "admin";
        String encodePassword = MD5Util.md5(username + password);
        PowerMockito.when(stringRedisTemplate.expire("login:" + token,7, TimeUnit.DAYS)).thenReturn(true);
        PowerMockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set("login:" + token, username);
        responseBean.setCode(200);
        responseBean.setData(new AccountInfo(username,  MD5Util.md5(encodePassword)));
        responseBean.setMsg("登录成功!");
        PowerMockito.when(accountDao.login(username,encodePassword)).thenReturn(account);
        ResponseBean responseBeanCorrect = accountService.login(username, password);
        Assert.assertEquals(responseBean.getCode(), responseBeanCorrect.getCode());

        /*
            错误的用户名及密码
         */
        username = "admin";
        password = "admins";
        responseBean.setCode(401);
        responseBean.setData(null);
        responseBean.setMsg("用户名或密码错误!");
        encodePassword = MD5Util.md5(username + password);
        PowerMockito.when(accountDao.login(username,encodePassword)).thenReturn(null);
        ResponseBean responseBeanIncorrect = accountService.login(username, password);
        Assert.assertEquals(responseBean.getCode(), responseBeanIncorrect.getCode());
    }

    @Test
    @Ignore
    public void getUserNameByToken() {

    }

    @Test
    @Ignore
    public void getAccountByToken() {

    }

    @Test
    @Ignore
    public void authByToken() {

    }

    @Test
    public void addAccountTest_Null() {
        account.setName(null);
        try{
            accountService.addAccount(account);
        }catch(Exception e){
            Assert.assertEquals("param loss",e.getMessage());
        }
    }

    @Test
    public void addAccountTest_AccountName() {
        PowerMockito.when(accountService.isAccountNameExist(account.getAccountName())).thenReturn(true);
        try{
            accountService.addAccount(account);
        }catch(Exception e){
            Assert.assertEquals("accountName has been used!",e.getMessage());
        }
    }

    @Test
    public void addAccountTest_Email() {
        PowerMockito.when(accountService.isAccountNameExist(account.getAccountName())).thenReturn(false);
        PowerMockito.when(accountService.isEmailExist(account.getEmail())).thenReturn(true);
        try{
            accountService.addAccount(account);
        }catch(Exception e){
            Assert.assertEquals("email has been used!",e.getMessage());
        }
    }

    @Test
    public void addAccountTest_Name() {
        PowerMockito.when(accountService.isAccountNameExist(account.getAccountName())).thenReturn(false);
        PowerMockito.when(accountService.isEmailExist(account.getEmail())).thenReturn(false);
        PowerMockito.when(accountService.isNameExist(account.getName())).thenReturn(true);
        try{
            accountService.addAccount(account);
        }catch(Exception e){
            Assert.assertEquals("nickName has been used!",e.getMessage());
        }
    }

    @Test
    public void addAccountTest_Success() {
        PowerMockito.when(accountService.isAccountNameExist(account.getAccountName())).thenReturn(false);
        PowerMockito.when(accountService.isEmailExist(account.getEmail())).thenReturn(false);
        PowerMockito.when(accountService.isNameExist(account.getName())).thenReturn(false);
        doNothing().when(accountDao).addAccount(account);
        try{
            accountService.addAccount(account);
        }catch(Exception e){
        }
        verify(accountDao, Mockito.atLeast(1)).addAccount(account);
    }

    @Test
    public void getAllAccountId() {
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        PowerMockito.when(accountDao.getAllAccountId()).thenReturn(list);
        List<String> listResult = accountService.getAllAccountId();
        Assert.assertEquals(list.size(), listResult.size());
        for(int i=0;i<listResult.size();i++){
            Assert.assertEquals(list.indexOf(i), listResult.indexOf(i));
        }
    }

    @Test
    public void isAccountNameExist() {
        /*
            accountName 已存在时
         */
        String accountName = "admin";
        PowerMockito.when(accountDao.isAccountNameExist(accountName)).thenReturn(true);
        Boolean result = accountService.isAccountNameExist(accountName);
        Assert.assertEquals(true, result);

        /*
            accountName 不存在时
         */
        PowerMockito.when(accountDao.isAccountNameExist(accountName)).thenReturn(false);
        result = accountService.isAccountNameExist(accountName);
        Assert.assertEquals(false, result);
    }

    @Test
    public void isNameExist() {
        /*
            Name 已存在时
         */
        String name = "acc";
        PowerMockito.when(accountDao.isNameExist(name)).thenReturn(true);
        Boolean result = accountService.isNameExist(name);
        Assert.assertEquals(true, result);

        /*
            Name 不存在时
         */
        PowerMockito.when(accountDao.isNameExist(name)).thenReturn(false);
        result = accountService.isNameExist(name);
        Assert.assertEquals(false, result);
    }

    @Test
    public void isEmailExist() {
        /*
            Email 已存在时
         */
        String email = "123@qq.com";
        PowerMockito.when(accountDao.isEmailExist(email)).thenReturn(true);
        Boolean result = accountService.isEmailExist(email);
        Assert.assertEquals(true, result);

        /*
            Email 不存在时
         */
        PowerMockito.when(accountDao.isEmailExist(email)).thenReturn(false);
        result = accountService.isEmailExist(email);
        Assert.assertEquals(false, result);
    }

}