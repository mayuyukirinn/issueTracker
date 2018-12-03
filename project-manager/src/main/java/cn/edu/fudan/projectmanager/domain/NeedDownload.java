package cn.edu.fudan.projectmanager.domain;

public class NeedDownload {


    private String projectId;
    private String url;
    private boolean isPrivate;
    private String username;
    private String password;


    public NeedDownload() {
    }

    public NeedDownload(String projectId, String url, boolean isPrivate, String username, String password) {
        this.projectId = projectId;
        this.url = url;
        this.isPrivate = isPrivate;
        this.username = username;
        this.password = password;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
