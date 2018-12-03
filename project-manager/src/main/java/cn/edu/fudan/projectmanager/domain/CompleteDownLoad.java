package cn.edu.fudan.projectmanager.domain;

public class CompleteDownLoad {

    private String projectId;
    private String language;
    private String vcs_type;
    private String status;
    private String description;

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    private String repo_id;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVcs_type() {
        return vcs_type;
    }

    public void setVcs_type(String vcs_type) {
        this.vcs_type = vcs_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
