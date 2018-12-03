package cn.edu.fudan.accountservice.service;


import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.ResponseBean;

import java.util.List;


public interface AccountService {

    ResponseBean login(String username, String password);

    String getUserNameByToken(String userToken);

    boolean isAccountNameExist(String accountName);

    boolean isNameExist(String name);

    boolean isEmailExist(String email);

    boolean authByToken(String userToken);

    Account getAccountByToken(String userToken);

    void addAccount(Account account);

    List<String> getAllAccountId();
}
