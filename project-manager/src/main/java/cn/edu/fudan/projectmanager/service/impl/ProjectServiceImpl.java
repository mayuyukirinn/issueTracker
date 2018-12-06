package cn.edu.fudan.projectmanager.service.impl;


import cn.edu.fudan.projectmanager.component.RestInterfaceManager;
import cn.edu.fudan.projectmanager.dao.*;
import cn.edu.fudan.projectmanager.domain.NeedDownload;
import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.service.ProjectService;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class ProjectServiceImpl implements ProjectService {

    private static Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Value("${github.api.path}")
    private String githubAPIPath;
    @Value("${repo.url.pattern}")
    private String repoUrlPattern;

    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private KafkaTemplate kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    private ProjectDao projectDao;

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @SuppressWarnings("unchecked")
    private void send(String projectId, String url,boolean isPrivate,String username,String password) {
        NeedDownload needDownload = new NeedDownload(projectId, url,isPrivate,username,password);
        kafkaTemplate.send("ProjectManager", JSONObject.toJSONString(needDownload));
        logger.info("send message to topic ProjectManage ---> " + JSONObject.toJSONString(needDownload));
    }

    private void checkProjectURL(String url,boolean isPrivate) {
        Pattern pattern = Pattern.compile(repoUrlPattern);
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            throw new RuntimeException("invalid url!");
        }
//        if(!isPrivate){
//            //前面做过一次全匹配，需要把matcher重置一下才能接着从头匹配
//            matcher.reset();
//            boolean isMavenProject = false;
//            while (matcher.find()) {
//                String author_projectName = matcher.group(1);
//                JSONArray fileList = restTemplate.getForEntity(githubAPIPath + author_projectName + "/contents", JSONArray.class).getBody();
//                if (fileList != null) {
//                    for (int i = 0; i < fileList.size(); i++) {
//                        JSONObject file = fileList.getJSONObject(i);
//                        if (file.getString("name").equals("pom.xml")) {
//                            isMavenProject = true;
//                            break;
//                        }
//                    }
//                    if (!isMavenProject)
//                        throw new RuntimeException("failed,this project is not maven project!");
//                } else {
//                    throw new RuntimeException("invalid url!");
//                }
//            }
//        }
    }

    @Override
    public void addOneProject(String userToken, JSONObject projectInfo) {
        String url = projectInfo.getString("url");
        if (url == null) {
            throw new RuntimeException("please input the project url!");
        }
        url = url.trim();
        boolean isPrivate=projectInfo.getBooleanValue("isPrivate");
        checkProjectURL(url,isPrivate);
        String username=projectInfo.getString("username");
        String password=projectInfo.getString("password");
        if(isPrivate){
            if(username==null||password==null){
                throw new RuntimeException("this project is private,please provide your git username and password!");
            }
        }
        Project project=new Project();
        String name=projectInfo.getString("name");
        String type=projectInfo.getString("type");
        String account_id = restInterfaceManager.getAccountId(userToken);
        if (projectDao.hasBeenAdded(account_id, url, type)) {
            throw new RuntimeException("The project has been added!");
        }
        List<Project> projects=projectDao.getProjectsByURLAndType(url,type);
        if(projects!=null&&!projects.isEmpty()){
            //如果存在其他用户的project和当前添加的URL和type相同，需要将扫描状态同步
            Project oneProject=projects.get(0);
            project.setScan_status(oneProject.getScan_status());
            project.setTill_commit_time(oneProject.getTill_commit_time());
            project.setLast_scan_time(oneProject.getLast_scan_time());
       }else{
            project.setScan_status("Not Scanned");
        }
        String projectId = UUID.randomUUID().toString();
        project.setUuid(projectId);
        project.setName(name);
        project.setUrl(url);
        project.setType(type);
        project.setVcs_type("git");
        project.setAccount_id(account_id);
        project.setDownload_status("Downloading");
        project.setAdd_time(new Date());
        projectDao.addOneProject(project);
        //向RepoManager这个Topic发送消息，请求开始下载
        send(projectId, url,isPrivate,username,password);
    }

    @Override
    public Object getProjectList(String userToken,String type) {
        String account_id = restInterfaceManager.getAccountId(userToken);
        return projectDao.getProjectList(account_id,type);
    }

    @Override
    public Object getProjectByRepoId(String repo_id) {
        return projectDao.getProjectByRepoId(repo_id);
    }

    @Override
    public Object getProjectByAccountId(String account_id) {
        return projectDao.getProjectByAccountId(account_id);
    }

    @Override
    public Object getProjectListByKeyWord(String userToken, String keyWord,String type) {
        String account_id = restInterfaceManager.getAccountId(userToken);
        return projectDao.getProjectByKeyWordAndAccountId(account_id, keyWord.trim(),type);
    }

    @Override
    public List<String> getRepoIdsByAccountIdAndType(String account_id,String type) {
        return projectDao.getRepoIdsByAccountIdAndType(account_id,type);
    }

    @Override
    public Project getProjectByID(String projectId) {
        return projectDao.getProjectByID(projectId);
    }

    @Override
    public void updateProjectStatus(Project project) {
        projectDao.updateProjectStatus(project);
    }


    private void updateProjectStatus(String projectId,String status){
        Project project=new Project();
        project.setUuid(projectId);
        project.setScan_status(status);
        projectDao.updateProjectStatus(project);
    }

    @Override
    public void remove(String projectId, String type,String userToken) {
        updateProjectStatus(projectId,"deleting");
        String repoId = restInterfaceManager.getRepoIdOfProject(projectId);
        if(repoId!=null){
            String account_id = restInterfaceManager.getAccountId(userToken);
            //如果当前repoId和type只有这一个projectId与其对应，那么删除project的同时会删除repo的相关内容
            //否则还有其他project与当前repoId和type对应，该repo的相关内容就不删
            if (!projectDao.existOtherProjectWithThisRepoIdAndType(repoId,type)) {
                restInterfaceManager.deleteIssuesOfRepo(repoId,type);
                restInterfaceManager.deleteRawIssueOfRepo(repoId,type);
                restInterfaceManager.deleteScanOfRepo(repoId,type);
                restInterfaceManager.deleteEventOfRepo(repoId,type);
                //delete info in redis
                stringRedisTemplate.setEnableTransactionSupport(true);
                stringRedisTemplate.multi();
                stringRedisTemplate.delete("dashboard:"+type+":day:" + repoId);
                stringRedisTemplate.delete("dashboard:"+type+":week:" + repoId);
                stringRedisTemplate.delete("dashboard:"+type+":month:" + repoId);
                stringRedisTemplate.delete("trend:"+type +":day:new:" + account_id + ":" + repoId);
                stringRedisTemplate.delete("trend:"+type+":day:remaining:" + account_id + ":" + repoId);
                stringRedisTemplate.delete("trend:"+type+":day:eliminated:" + account_id + ":" + repoId);
                stringRedisTemplate.delete("trend:"+type+":week:new:" + account_id + ":" + repoId);
                stringRedisTemplate.delete("trend:"+type+":week:remaining:" + account_id + ":" + repoId);
                stringRedisTemplate.delete("trend:"+type+":week:eliminated:" + account_id + ":" + repoId);
                stringRedisTemplate.exec();
            }
        }
        projectDao.remove(projectId);
        logger.info("project delete success!");
    }

    @Override
    public String getRepoId(String projectId) {
        return projectDao.getRepoId(projectId);
    }

    @Override
    public Object existProjectWithThisRepoIdAndType(String repoId, String type) {
        Map<String,Object> result=new HashMap<>();
        result.put("exist",projectDao.existProjectWithThisRepoIdAndType(repoId, type));
        return result;
    }
}