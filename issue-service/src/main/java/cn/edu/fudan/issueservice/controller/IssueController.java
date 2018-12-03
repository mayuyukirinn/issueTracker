package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.domain.IssueParam;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.IssueService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class IssueController {


    private IssueService issueService;

    @Autowired
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }


    @GetMapping(value = {"/issue"})
    public Object getIssues(@RequestParam("project_id") String project_id,
                            @RequestParam("page") Integer page,
                            @RequestParam("size") Integer size,
                            @RequestParam("category")String category) {
        return issueService.getIssueList(project_id, page, size,category);
    }

    @GetMapping(value = {"/issue/issue-types"})
    public Object getExistIssueTypes(@RequestParam(name = "category",defaultValue = "bug")String category) {
        return issueService.getExistIssueTypes(category);
    }

    //参数超过三个不宜采用requestParameter、post请求（参数封装成一个类自动解析）、get+requestBody
    @PostMapping(value = {"/issue/filter"})
    public Object filterIssues(@RequestBody JSONObject requestParam) {
        return issueService.getFilteredIssueList(requestParam);
    }

    @GetMapping(value = {"/issue/dashboard"})
    public Object getDashBoardInfo(@RequestParam("duration") String duration,
                                   @RequestParam(name = "project_id", required = false) String project_id,
                                   @RequestParam(name="category",required = false,defaultValue = "bug")String category,
                                   HttpServletRequest request) {
        String userToken = request.getHeader("token");
        return issueService.getDashBoardInfo(duration, project_id, userToken,category);
    }

    @GetMapping(value = {"/issue/project-statistical-info"})
    public Object getAvgEliminatedTimeAndMaxAliveTime(@RequestParam(name = "project_id") String project_id,
                                                      @RequestParam(name="category",defaultValue = "bug")String category){
        return issueService.getAliveAndEliminatedInfo(project_id, category);
    }

    @GetMapping(value = {"/issue/statistical-results"})
    public Object getStatisticalResults(@RequestParam("month") Integer month,
                                        @RequestParam(name = "project_id", required = false) String project_id,
                                        @RequestParam(name="category",required = false,defaultValue = "bug")String category,
                                        HttpServletRequest request) {
        String userToken = request.getHeader("token");
        return issueService.getStatisticalResults(month, project_id, userToken,category);
    }

    @PostMapping("/issue/specific-issues")
    public Object getSpecificIssues(@RequestBody IssueParam issueParam){
        return issueService.getSpecificIssues(issueParam);
    }

    //下面都是供其他服务调用的内部接口

    @PostMapping(value = {"/inner/issue"})
    public Object addIssues(@RequestBody List<Issue> issueList) {
        try {
            issueService.insertIssueList(issueList);
            return new ResponseBean(200, "issues add success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "issues add failed!", null);
        }
    }

    @DeleteMapping(value = {"/inner/issue/{category}/{repoId}"})
    public Object deleteIssues(@PathVariable("category")String category,@PathVariable("repoId") String repoId) {
        try {
            issueService.deleteIssueByRepoIdAndCategory(repoId,category);
            return new ResponseBean(200, "issues delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "issues delete failed!", null);
        }
    }

    @PutMapping(value = {"/inner/issue"})
    public Object updateIssues(@RequestBody List<Issue> issueList) {
        try {
            issueService.batchUpdateIssue(issueList);
            return new ResponseBean(200, "issues update success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "issues update failed!", null);
        }
    }

    @PostMapping(value = {"/inner/issue/mapping"})
    public Object mapping(@RequestBody JSONObject requestParam) {
        try {
            String repo_id = requestParam.getString("repo_id");
            String pre_commit_id = requestParam.getString("pre_commit_id");
            String current_commit_id = requestParam.getString("current_commit_id");
            String category=requestParam.getString("category");
            issueService.startMapping(repo_id, pre_commit_id, current_commit_id,category);
            return new ResponseBean(200, "issues mapping success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "issues mapping failed!", null);
        }
    }

    @GetMapping(value = "/inner/issue/dashboard")
    public Object updateIssueCount(@RequestParam ("time") String time){
        try {
            issueService.updateIssueCount(time);
            return new ResponseBean(200, "update success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, " update failed!", null);
        }
    }

}
