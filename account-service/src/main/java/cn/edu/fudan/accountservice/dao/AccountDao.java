package cn.edu.fudan.accountservice.dao;

import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccountDao {


    private AccountMapper accountMapper;

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    public Account login(String accountName, String password) {
        return accountMapper.login(accountName, password);
    }

    public Account getAccountByAccountName(String accountName) {
        return accountMapper.getAccountByAccountName(accountName);
    }

    public boolean isAccountNameExist(String accountName) {
        return getAccountByAccountName(accountName) != null;
    }

    public boolean isNameExist(String name) {
        return accountMapper.getAccountIdByName(name) != null;
    }

    public boolean isEmailExist(String email) {
        return accountMapper.getAccountByEmail(email) != null;
    }

    public void addAccount(Account account) {
        accountMapper.addAccount(account);
    }

    public List<String> getAllAccountId() {
        return accountMapper.getAllAccountId();
    }
}
