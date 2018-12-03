package cn.edu.fudan.accountservice.mapper;


import cn.edu.fudan.accountservice.domain.Account;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountMapper {

    Account login(@Param("accountName") String accountName, @Param("password") String password);

    void addAccount(Account account);

    String getAccountIdByName(@Param("name") String name);

    Account getAccountByEmail(@Param("email") String email);

    Account getAccountByAccountName(String accountName);

    List<String> getAllAccountId();

}
