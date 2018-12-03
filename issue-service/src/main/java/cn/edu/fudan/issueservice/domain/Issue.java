package cn.edu.fudan.issueservice.domain;

import java.util.Date;
import java.util.List;

public class Issue {

    private String uuid;
    private String type;
    private String category;
    private String start_commit;
    private Date start_commit_date;
    private String end_commit;
    private Date end_commit_date;
    private String raw_issue_start;
    private String raw_issue_end;
    private String repo_id;
    private String target_files;
    private Date create_time;
    private Date update_time;
    private IssueType issueType;
    private List<Object> tags;

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public Issue() {
    }

    public Issue(String uuid, String type, String category, String start_commit, Date start_commit_date, String end_commit, Date end_commit_date, String raw_issue_start, String raw_issue_end, String repo_id, String target_files, Date create_time, Date update_time) {
        this.uuid = uuid;
        this.type = type;
        this.category = category;
        this.start_commit = start_commit;
        if(start_commit_date == null){
            this.start_commit_date = null;
        }else {
            this.start_commit_date = (Date) start_commit_date.clone();
        }
        this.end_commit = end_commit;
        if(end_commit_date == null){
            this.end_commit_date = null;
        }else {
            this.end_commit_date = (Date) end_commit_date.clone();
        }
        this.raw_issue_start = raw_issue_start;
        this.raw_issue_end = raw_issue_end;
        this.repo_id = repo_id;
        this.target_files = target_files;
        if(create_time == null){
            this.create_time = null;
        }else {
            this.create_time = (Date) create_time.clone();
        }
        if(update_time == null){
            this.update_time = null;
        }else {
            this.update_time = (Date) update_time.clone();
        }
    }

    public Date getStart_commit_date() {
        return start_commit_date;
    }

    public void setStart_commit_date(Date start_commit_date) {
        this.start_commit_date = start_commit_date;
    }

    public Date getEnd_commit_date() {
        return end_commit_date;
    }

    public void setEnd_commit_date(Date end_commit_date) {
        this.end_commit_date = end_commit_date;
    }

    public Date getCreate_time() {
        if(create_time == null){
            return null;
        }
        return (Date)create_time.clone();
    }

    public void setCreate_time(Date create_time) {
        if(create_time == null){
            this.create_time = null;
        }else {
            this.create_time = (Date) create_time.clone();
        }
    }

    public Date getUpdate_time() {
        if(update_time == null){
            return null;
        }
        return (Date)update_time.clone();
    }

    public void setUpdate_time(Date update_time) {
        if(update_time == null){
            this.update_time = null;
        }else {
            this.update_time = (Date) update_time.clone();
        }
    }

    public List<Object> getTags() {
        return tags;
    }

    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStart_commit() {
        return start_commit;
    }

    public void setStart_commit(String start_commit) {
        this.start_commit = start_commit;
    }

    public String getEnd_commit() {
        return end_commit;
    }

    public void setEnd_commit(String end_commit) {
        this.end_commit = end_commit;
    }

    public String getRaw_issue_start() {
        return raw_issue_start;
    }

    public void setRaw_issue_start(String raw_issue_start) {
        this.raw_issue_start = raw_issue_start;
    }

    public String getRaw_issue_end() {
        return raw_issue_end;
    }

    public void setRaw_issue_end(String raw_issue_end) {
        this.raw_issue_end = raw_issue_end;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    public String getTarget_files() {
        return target_files;
    }

    public void setTarget_files(String target_files) {
        this.target_files = target_files;
    }
}
