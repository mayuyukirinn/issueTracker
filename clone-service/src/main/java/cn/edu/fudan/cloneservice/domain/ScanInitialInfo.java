package cn.edu.fudan.cloneservice.domain;

/**
 * @author WZY
 * @version 1.0
 **/
public class ScanInitialInfo {

    private Scan scan;
    private String repoName;
    private String repoId;
    private String repoPath;

    public ScanInitialInfo(Scan scan, String repoName, String repoId, String repoPath) {
        this.scan = scan;
        this.repoName = repoName;
        this.repoId = repoId;
        this.repoPath = repoPath;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }
}
