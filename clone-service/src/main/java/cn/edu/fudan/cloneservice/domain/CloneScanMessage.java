package cn.edu.fudan.cloneservice.domain;

/**
 * @author WZY
 * @version 1.0
 **/
public class CloneScanMessage {

    private String repoName;
    private String repoPath;
    private String commitId;

    public CloneScanMessage() {
    }

    public CloneScanMessage(String repoName, String repoPath, String commitId) {
        this.repoName = repoName;
        this.repoPath = repoPath;
        this.commitId = commitId;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
}
