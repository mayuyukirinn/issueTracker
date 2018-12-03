package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.domain.ResponseBean;
import cn.edu.fudan.projectmanager.service.ProjectService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ProjectController {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }


    @PostMapping(value = {"/project"})
    public Object addProject(HttpServletRequest request, @RequestBody JSONObject projectInfo) {
        String userToken = request.getHeader("token");
        try {
            projectService.addOneProject(userToken, projectInfo);
            return new ResponseBean(200, "add success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "add failed :" + e.getMessage(), null);
        }
    }

    //get project list
    @GetMapping(value = {"/project"})
    public Object query(HttpServletRequest request,
                        @RequestParam(name = "type",required = false,defaultValue = "bug")String type) {
        String userToken = request.getHeader("token");
        return projectService.getProjectList(userToken,type);
    }

    @GetMapping(value = {"/project/filter"})
    public Object keyWordQuery(HttpServletRequest request,
                               @RequestParam("keyWord") String keyWord,
                               @RequestParam(name = "type",required = false,defaultValue = "bug")String type) {
        String userToken = request.getHeader("token");
        return projectService.getProjectListByKeyWord(userToken, keyWord,type);
    }

    @DeleteMapping(value = {"/project/{projectId}"})
    public Object delete(@PathVariable("projectId") String projectId,
                         @RequestParam(name = "type",required = false,defaultValue = "bug")String type,
                         HttpServletRequest request) {
        try {
            projectService.remove(projectId, type,request.getHeader("token"));
            return new ResponseBean(200, "project delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "project delete failed!", null);
        }
    }

    //下面是其它服务调用的内部接口

    @PutMapping(value = {"/inner/project"})
    public Object updateProject(@RequestBody Project project) {
        try {
            projectService.updateProjectStatus(project);
            return new ResponseBean(200, "project update success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "project update failed!", null);
        }
    }

    @GetMapping(value = {"/inner/project"})
    public Object getProjectByRepoId(@RequestParam("repo_id") String repo_id){
        return projectService.getProjectByRepoId(repo_id);
    }

    @GetMapping(value = {"/inner/project/{project-id}"})
    public Object getProject(@PathVariable("project-id") String project_id) {
        return projectService.getProjectByID(project_id);
    }

    @GetMapping(value = "/inner/project/repo-id")
    public String getRepoId(@RequestParam("project-id") String projectId) {
        return projectService.getRepoId(projectId);
    }

    @GetMapping(value = "/inner/project/repo-ids")
    public Object getProjectIds(@RequestParam(name = "account_id", required = false) String account_id,
                                @RequestParam("type")String type) {
        return projectService.getRepoIdsByAccountIdAndType(account_id,type);
    }

    @GetMapping(value = "/inner/projects")
    public Object getProjectByAccountId(@RequestParam(name = "account_id", required = false) String account_id){
        return projectService.getProjectByAccountId(account_id);
    }


}
