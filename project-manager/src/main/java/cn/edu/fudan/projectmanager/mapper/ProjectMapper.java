package cn.edu.fudan.projectmanager.mapper;

import cn.edu.fudan.projectmanager.domain.Project;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectMapper {

    void addOneProject(Project project);

    List<Project> getProjectByAccountId(@Param("account_id") String account_id);

    List<Project> getProjectList(@Param("account_id") String account_id,@Param("type") String type);

    List<Project> getProjectByKeyWordAndAccountId(@Param("account_id") String account_id, @Param("keyWord") String keyWord,@Param("type") String type);

    List<Project> getProjectByRepoId(@Param("repo_id") String repo_id);

    Project getProjectByID(String uuid);

    Project getProjectByURLTypeAndAccountId(@Param("account_id") String account_id, @Param("url") String url, @Param("type") String type);

    List<Project> getProjectsByURLAndType(@Param("url") String url, @Param("type") String type);

    void updateProjectStatus(Project project);

    void remove(@Param("projectId") String projectId);

    String getRepoId(@Param("projectId") String projectId);

    List<String> getRepoIdsByAccountIdAndType(@Param("account_id") String account_id,@Param("type") String type);

    List<String> getProjectIdsByRepoIdAndType(@Param("repo_id") String repo_id,@Param("type") String type);
}
