package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.Issue;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface IssueMapper {

    void insertIssueList(List<Issue> list);

    void deleteIssueByRepoIdAndCategory(@Param("repo_id")String repo_id,@Param("category")String category);

    void batchUpdateIssue(List<Issue> list);

    Issue getIssueByID(String uuid);

    int getSpecificIssueCount(Map<String, Object> map);

    List<Issue> getSpecificIssues(Map<String, Object> map);

    Integer getIssueCount(Map<String, Object> map);

    List<Issue> getSolvedIssues(@Param("repo_id") String repo_id, @Param("commit_id") String commit_id);

    List<Issue> getIssueList(Map<String, Object> map);

    List<String> getExistIssueTypes(@Param("category")String category);

    List<String> getIssueIdsByRepoIdAndCategory(@Param("repo_id")String repo_id,@Param("category")String category);

    Double getAvgEliminatedTime(@Param("list")List<String> list, @Param("repo_id")String repo_id, @Param("category")String category);

    Long getMaxAliveTime(@Param("list")List<String> list, @Param("repo_id")String repo_id, @Param("category")String category);
}
