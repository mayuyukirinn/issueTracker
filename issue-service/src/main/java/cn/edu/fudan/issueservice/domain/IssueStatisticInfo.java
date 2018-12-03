package cn.edu.fudan.issueservice.domain;

/**
 * @author WZY
 * @version 1.0
 **/
public class IssueStatisticInfo {

    private double avgEliminatedTime;
    private long maxAliveTime;

    public IssueStatisticInfo() {
    }

    public IssueStatisticInfo(double avgEliminatedTime, long maxAliveTime) {
        this.avgEliminatedTime = avgEliminatedTime;
        this.maxAliveTime = maxAliveTime;
    }

    public double getAvgEliminatedTime() {
        return avgEliminatedTime;
    }

    public void setAvgEliminatedTime(double avgEliminatedTime) {
        this.avgEliminatedTime = avgEliminatedTime;
    }

    public long getMaxAliveTime() {
        return maxAliveTime;
    }

    public void setMaxAliveTime(long maxAliveTime) {
        this.maxAliveTime = maxAliveTime;
    }
}
