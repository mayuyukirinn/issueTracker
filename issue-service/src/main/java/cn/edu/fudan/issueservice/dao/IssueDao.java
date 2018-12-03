package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.mapper.IssueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public class IssueDao {

    private IssueMapper issueMapper;

    @Autowired
    public void setIssueMapper(IssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    public void insertIssueList(List<Issue> list) {
        issueMapper.insertIssueList(list);
    }

    public void deleteIssueByRepoIdAndCategory(String repo_id,String category) {
        issueMapper.deleteIssueByRepoIdAndCategory(repo_id,category);
    }

    public void batchUpdateIssue(List<Issue> list) {
        issueMapper.batchUpdateIssue(list);
    }

    public Issue getIssueByID(String uuid) {
        return issueMapper.getIssueByID(uuid);
    }

    public Integer getIssueCount(Map<String, Object> map) {
        return issueMapper.getIssueCount(map);
    }

    public List<Issue> getIssueList(Map<String, Object> map) {
        return issueMapper.getIssueList(map);
    }

    public List<String> getExistIssueTypes(String category) {
        return issueMapper.getExistIssueTypes(category);
    }

    public List<String> getIssueIdsByRepoIdAndCategory(String repo_id,String category) {
        return issueMapper.getIssueIdsByRepoIdAndCategory(repo_id,category);
    }

    public List<Issue> getSolvedIssues(String repo_id, String commit_id) {
        return issueMapper.getSolvedIssues(repo_id, commit_id);
    }


    public int getSpecificIssueCount(Map<String, Object> map){
        return issueMapper.getSpecificIssueCount(map);
    }

    public List<Issue> getSpecificIssues(Map<String, Object> map){
        return issueMapper.getSpecificIssues(map);
    }

    public Double getAvgEliminatedTime(List<String> list,String repo_id,String category){
        return issueMapper.getAvgEliminatedTime(list, repo_id, category);
    }

    public Long getMaxAliveTime(List<String> list, String repo_id, String category){
        return issueMapper.getMaxAliveTime(list, repo_id, category);
    }
}
