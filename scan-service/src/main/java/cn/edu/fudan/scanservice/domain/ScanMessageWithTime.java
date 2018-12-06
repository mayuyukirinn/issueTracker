package cn.edu.fudan.scanservice.domain;

import java.util.Date;

/**
 * @author WZY
 * @version 1.0
 **/
public class ScanMessageWithTime {

    private String repoId;
    private String commitId;
    private Date commitTime;

    public ScanMessageWithTime() {
    }

    public ScanMessageWithTime(String repoId, String commitId) {
        this.repoId = repoId;
        this.commitId = commitId;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public Date getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(Date commitTime) {
        this.commitTime = commitTime;
    }

    @Override
    public String toString() {
        return "ScanMessageWithTime{" +
                "repoId='" + repoId + '\'' +
                ", commitId='" + commitId + '\'' +
                ", commitTime=" + commitTime +
                '}';
    }
}
