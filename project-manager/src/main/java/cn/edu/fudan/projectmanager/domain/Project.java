package cn.edu.fudan.projectmanager.domain;

import java.util.Date;

public class Project {

    private String uuid;
    private String name;
    private String language;
    private String url;
    private String vcs_type;
    private String type;
    private String account_id;
    private String download_status;
    private String scan_status;
    private Date add_time;
    private Date till_commit_time;
    private Date last_scan_time;
    private String description;
    private String repo_id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getAdd_time() {
        if(add_time == null){
            return null;
        }
        return (Date)add_time.clone();
    }

    public void setAdd_time(Date add_time) {
        if(add_time == null){
            this.add_time = null;
        }else{
            this.add_time = (Date) add_time.clone();
        }
    }

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVcs_type() {
        return vcs_type;
    }

    public void setVcs_type(String vcs_type) {
        this.vcs_type = vcs_type;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getDownload_status() {
        return download_status;
    }

    public void setDownload_status(String download_status) {
        this.download_status = download_status;
    }

    public String getScan_status() {
        return scan_status;
    }

    public void setScan_status(String scan_status) {
        this.scan_status = scan_status;
    }

    public Date getTill_commit_time() {
        if(till_commit_time == null){
            return null;
        }
        return (Date) till_commit_time.clone();
    }

    public void setTill_commit_time(Date till_commit_time) {
        if(till_commit_time == null){
            this.till_commit_time = null;
        }else {
            this.till_commit_time = (Date) till_commit_time.clone();
        }
    }

    public Date getLast_scan_time() {
        if(last_scan_time == null){
            return null;
        }
        return (Date) last_scan_time.clone();
    }

    public void setLast_scan_time(Date last_scan_time) {
        if(last_scan_time == null){
            this.last_scan_time = null;
        }else {
            this.last_scan_time = (Date) last_scan_time.clone();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
