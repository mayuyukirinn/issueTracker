package cn.edu.fudan.projectmanager.service;


import cn.edu.fudan.projectmanager.domain.Project;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface ProjectService {

    void addOneProject(String userToken, JSONObject projectInfo);

    Object getProjectList(String userToken,String type);

    Object getProjectByRepoId(String repo_id);

    Object getProjectByAccountId(String account_id);

    Object getProjectListByKeyWord(String userToken, String keyWord,String type);

    List<String> getRepoIdsByAccountIdAndType(String account_id,String type);

    Project getProjectByID(String projectId);

    void updateProjectStatus(Project project);

    void remove(String projectId,String type, String userToken);

    String getRepoId(String projectId);

    Object existProjectWithThisRepoIdAndType(String repoId,String type);
}
