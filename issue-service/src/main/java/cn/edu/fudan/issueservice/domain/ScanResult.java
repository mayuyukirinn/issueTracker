package cn.edu.fudan.issueservice.domain;

import java.util.Date;

/**
 * @author WZY
 * @version 1.0
 **/
public class ScanResult {

    private int id;
    private String category;
    private String repo_id;
    private Date scan_date;
    private Date commit_date;
    private int new_count;
    private int eliminated_count;
    private int remaining_count;

    public ScanResult() {
    }

    public ScanResult(String category, String repo_id, Date scan_date, Date commit_date, int new_count, int eliminated_count, int remaining_count) {
        this.category = category;
        this.repo_id = repo_id;
        this.scan_date=scan_date;
        this.commit_date = commit_date;
        this.new_count = new_count;
        this.eliminated_count = eliminated_count;
        this.remaining_count = remaining_count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    public Date getScan_date() {
        return scan_date;
    }

    public void setScan_date(Date scan_date) {
        this.scan_date = scan_date;
    }

    public Date getCommit_date() {
        return commit_date;
    }

    public void setCommit_date(Date commit_date) {
        this.commit_date = commit_date;
    }

    public int getNew_count() {
        return new_count;
    }

    public void setNew_count(int new_count) {
        this.new_count = new_count;
    }

    public int getEliminated_count() {
        return eliminated_count;
    }

    public void setEliminated_count(int eliminated_count) {
        this.eliminated_count = eliminated_count;
    }

    public int getRemaining_count() {
        return remaining_count;
    }

    public void setRemaining_count(int remaining_count) {
        this.remaining_count = remaining_count;
    }
}
