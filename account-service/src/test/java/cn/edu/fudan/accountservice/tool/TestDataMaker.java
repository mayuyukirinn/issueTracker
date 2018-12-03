package cn.edu.fudan.accountservice.tool;

import cn.edu.fudan.accountservice.domain.Account;

public class TestDataMaker {

    public static Account accountMaker(){
        Account account = new Account();
        account.setUuid("acc1");
        account.setName("acc");
        account.setAccountName("admin");
        account.setPassword("admin");
        account.setEmail("123@qq.com");
        return account;
    }
}
