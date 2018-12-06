package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectDao {


    private ProjectMapper projectMapper;

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public void addOneProject(Project project) {
        projectMapper.addOneProject(project);
    }

    public List<Project> getProjectByAccountId(String accountId) {
        return projectMapper.getProjectByAccountId(accountId);
    }

    public List<Project> getProjectByKeyWordAndAccountId(String account_id, String keyWord,String type) {
        return projectMapper.getProjectByKeyWordAndAccountId(account_id, keyWord,type);
    }

    public List<Project> getProjectByRepoId(String repo_id){
        return projectMapper.getProjectByRepoId(repo_id);
    }

    public List<Project> getProjectList(String account_id,String type){
        return projectMapper.getProjectList(account_id, type);
    }

    public Project getProjectByID(String projectId) {
        return projectMapper.getProjectByID(projectId);
    }

    public boolean hasBeenAdded(String account_id, String url, String type) {
        return projectMapper.getProjectByURLTypeAndAccountId(account_id, url, type) != null;
    }

    public void updateProjectStatus(Project project) {
        projectMapper.updateProjectStatus(project);
    }

    public void remove(String projectId) {
        projectMapper.remove(projectId);
    }

    public String getRepoId(String projectId) {
        return projectMapper.getRepoId(projectId);
    }


    public boolean existOtherProjectWithThisRepoIdAndType(String repoId,String type) {
        return projectMapper.getProjectIdsByRepoIdAndType(repoId,type).size() >= 2;
    }

    public boolean existProjectWithThisRepoIdAndType(String repoId,String type){
        return !projectMapper.getProjectIdsByRepoIdAndType(repoId,type).isEmpty();
    }

    public List<Project> getProjectsByURLAndType(String url,String type){
        return projectMapper.getProjectsByURLAndType(url, type);
    }

    public List<String> getRepoIdsByAccountIdAndType(String account_id,String type){
        return projectMapper.getRepoIdsByAccountIdAndType(account_id,type);
    }
}
