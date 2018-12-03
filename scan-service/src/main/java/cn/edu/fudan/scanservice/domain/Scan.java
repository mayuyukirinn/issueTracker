package cn.edu.fudan.scanservice.domain;

import java.util.Date;

public class Scan {

    private String uuid;
    private String category;
    private String name;
    private Date start_time;
    private Date end_time;
    private String status;
    private String result_summary;
    private String repo_id;
    private String commit_id;
    private Date commit_time;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart_time() {
        if(start_time == null){
            return null;
        }
        return (Date) start_time.clone();
    }

    public void setStart_time(Date start_time) {
        if(start_time == null){
            this.start_time = null;
        }else {
            this.start_time = (Date) start_time.clone();
        }
    }

    public Date getEnd_time() {
        if(end_time == null){
            return null;
        }
        return (Date) end_time.clone();
    }

    public void setEnd_time(Date end_time) {
        if(end_time == null){
            this.end_time = null;
        }else {
            this.end_time = (Date) end_time.clone();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult_summary() {
        return result_summary;
    }

    public void setResult_summary(String result_summary) {
        this.result_summary = result_summary;
    }

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    public String getCommit_id() {
        return commit_id;
    }

    public void setCommit_id(String commit_id) {
        this.commit_id = commit_id;
    }

    public Date getCommit_time() {
        if(commit_time == null){
            return null;
        }
        return (Date)commit_time.clone();
    }

    public void setCommit_time(Date commit_time) {
        if(commit_time == null){
            this.commit_time = null;
        }else {
            this.commit_time = (Date) commit_time.clone();
        }
    }
}
