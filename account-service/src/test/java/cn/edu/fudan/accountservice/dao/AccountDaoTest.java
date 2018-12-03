package cn.edu.fudan.accountservice.dao;

import cn.edu.fudan.accountservice.AccountServiceApplicationTests;
import cn.edu.fudan.accountservice.domain.Account;

import cn.edu.fudan.accountservice.tool.MockTestConnection;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Statement;
import java.util.List;

public class AccountDaoTest extends AccountServiceApplicationTests {

    @Autowired
    AccountDao accountDao;

    public static MockTestConnection mockTestConnection;

//    @BeforeClass
//    public static void setupConnection() throws Exception {
//        mockTestConnection = new MockTestConnection();
//        mockTestConnection.setupCoon();
//    }

//    @Before
//    public void setupData() throws Exception {
//        String sql = "insert into account values(\"3\",\"admin\",\"f6fdffe48c908deb0f4c3bd36c032e72\",\"admin\",\"123456@fudan.edu.cn\")";
//        Statement stmt = mockTestConnection.getConn().createStatement();//创建一个Statement对象
//        stmt.executeUpdate(sql);//执行sql语句
//        System.out.println("finish mocking");
//    }

//    @Test
//    public void getAccountIdByAccountName() {
//        /*
//            正确的用户名
//         */
//        String accountIdCorrect = accountDao.getAccountIdByAccountName("admin");
//        System.out.println(accountIdCorrect);
//
//        /*
//            错误的用户名
//         */
//        String accountIdIncorrect = accountDao.getAccountIdByAccountName("admins");
//        System.out.println(accountIdIncorrect);
//    }

    @Test
    @Ignore
    public void login() {
        /*
            正确的用户名及密码
         */
        Account accountCorrect = accountDao.login("admin", "f6fdffe48c908deb0f4c3bd36c032e72");
        System.out.println(accountCorrect.getAccountName() + " " + accountCorrect.getPassword());

        /*
            错误的用户名及密码
         */
        Account accountIncorrect = accountDao.login("admins", "123");
        System.out.println(accountIncorrect);
    }

    @Ignore
    @Test
    @Transactional
    public void addAccount() {
    }

    @Test
    @Ignore
    public void getAllAccountId() {
        List<String> listString = accountDao.getAllAccountId();
        for (String id : listString) {
            System.out.println(id);
        }
    }

//    @After
//    public void cleanData() throws Exception {
//        String sql = "delete from account where uuid='3'";
//        Statement stmt = mockTestConnection.getConn().createStatement();//创建一个Statement对象
//        stmt.executeUpdate(sql);//执行sql语句
//        System.out.println("finish cleaning");
//    }

//    @AfterClass
//    public static void closeConnection() throws Exception {
//        mockTestConnection.closeCoon();
//        System.out.println("关闭数据库成功");
//    }
}