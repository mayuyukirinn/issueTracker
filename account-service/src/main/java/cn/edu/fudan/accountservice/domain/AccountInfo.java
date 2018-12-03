package cn.edu.fudan.accountservice.domain;

/**
 * @author WZY
 * @version 1.0
 **/
public class AccountInfo {

    private String username;

    private String token;

    public AccountInfo() {
    }

    public AccountInfo(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
