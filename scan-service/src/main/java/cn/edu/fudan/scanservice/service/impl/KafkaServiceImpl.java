package cn.edu.fudan.scanservice.service.impl;


import cn.edu.fudan.scanservice.component.RestInterfaceManager;
import cn.edu.fudan.scanservice.domain.ScanMessage;
import cn.edu.fudan.scanservice.domain.ScanMessageWithTime;
import cn.edu.fudan.scanservice.domain.ScanResult;
import cn.edu.fudan.scanservice.service.KafkaService;
import cn.edu.fudan.scanservice.task.CloneScanTask;
import cn.edu.fudan.scanservice.task.FindBugScanTask;
import cn.edu.fudan.scanservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author WZY
 * @version 1.0
 **/
@Service
public class KafkaServiceImpl implements KafkaService {

    private Logger logger = LoggerFactory.getLogger(ScanServiceImpl.class);

    private FindBugScanTask findBugScanTask;
    private CloneScanTask cloneScanTask;
    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public KafkaServiceImpl(FindBugScanTask findBugScanTask,
                            CloneScanTask cloneScanTask,
                            RestInterfaceManager restInterfaceManager) {
        this.findBugScanTask = findBugScanTask;
        this.cloneScanTask = cloneScanTask;
        this.restInterfaceManager=restInterfaceManager;
    }

    //初始化project的一些状态,表示目前正在scan
    private void initialProject(String projectId) {
        JSONObject postData = new JSONObject();
        postData.put("uuid", projectId);
        postData.put("scan_status", "Scanning");
        restInterfaceManager.updateProject(postData);
    }



    private void updateProjects(String repo_id, JSONObject projectParam,String expected_type) {
        try {
            JSONArray projects = restInterfaceManager.getProjectsOfRepo(repo_id);
            if (projects != null && !projects.isEmpty()) {
                for (int i = 0; i < projects.size(); i++) {
                    JSONObject project=projects.getJSONObject(i);
                    String project_id = project.getString("uuid");
                    String type=project.getString("type");
                    if(type.equals(expected_type)){
                        projectParam.put("uuid", project_id);
                        restInterfaceManager.updateProject(projectParam);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("project update failed!");
        }
    }

    private void setTimeOut(Future<?> future, String repoId) {
        new Thread(() -> {
            try {
                future.get(15, TimeUnit.MINUTES);//设置15分钟的超时时间
            } catch (TimeoutException e) {
                //因scan超时而抛出异常
                logger.error("超时了");
                future.cancel(false);
                JSONObject projectParam = new JSONObject();
                projectParam.put("scan_status", "Scan Time Out");
                updateProjects(repoId, projectParam,"findbug");
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 通过前台请求触发scan操作
     *
     * @author WZY
     */
    @SuppressWarnings("unchecked")
    @Override
    public void scanByRequest(JSONObject requestParam) {
        String category=requestParam.getString("category");
        if(category==null){
            category="bug";
        }
        String projectId = requestParam.getString("projectId");
        String commitId = requestParam.getString("commitId");
        if(projectId==null||projectId.equals(""))
            throw new IllegalArgumentException("please provide projectId");
        if(commitId==null||commitId.equals(""))
            throw new IllegalArgumentException("please provide commitId");
        initialProject(projectId);
        String repoId = restInterfaceManager.getRepoIdOfProject(projectId);
        if(category.equals("clone")){
            Future<String> future =cloneScanTask.run(repoId,commitId,category);
            setTimeOut(future, repoId);
        }else{
            Future<String> future = findBugScanTask.run(repoId, commitId,category);
            //开一个工作者线程来管理异步任务的超时
            setTimeOut(future, repoId);
        }
    }

    /**
     * 监听消息队列中project的scan消息触发scan操作,暂时没有用到
     *
     * @author WZY
     */
    @Override
    @KafkaListener(id = "projectScan", topics = {"Scan"}, groupId = "scan")
    public void scanByMQ(ConsumerRecord<String, String> consumerRecord) {
        String msg = consumerRecord.value();
        logger.info("received message from topic -> " + consumerRecord.topic() + " : " + msg);
        ScanMessage scanMessage = JSONObject.parseObject(msg, ScanMessage.class);
        String repoId = scanMessage.getRepoId();
        String commitId = scanMessage.getCommitId();
        //串行扫
        if(existProject(repoId,"bug"))
            findBugScanTask.runSynchronously(repoId, commitId,"bug");
        if(existProject(repoId,"clone"))
            cloneScanTask.runSynchronously(repoId,commitId,"clone");
    }

    @KafkaListener(id = "updateCommit", topics = {"UpdateCommit"}, groupId = "updateCommit")
    public void firstScanByMQ(ConsumerRecord<String, String> consumerRecord){
        String msg = consumerRecord.value();
        List<ScanMessageWithTime> commits=JSONArray.parseArray(msg,ScanMessageWithTime.class);
        int size=commits.size();
        logger.info("received message from topic -> " + consumerRecord.topic() + " : " + size+" commits need to scan!");
        if(!commits.isEmpty()){
            List<ScanMessageWithTime> filteredCommits=getFilteredList(commits);
            String repoId=filteredCommits.get(0).getRepoId();
            if(existProject(repoId,"bug")){
                for(ScanMessageWithTime message:filteredCommits){
                    String commitId = message.getCommitId();
                    findBugScanTask.runSynchronously(repoId,commitId,"bug");
                }
            }
            if(existProject(repoId,"clone")){
                for(ScanMessageWithTime message:filteredCommits){
                    String commitId = message.getCommitId();
                    cloneScanTask.runSynchronously(repoId,commitId,"clone");
                }
            }
        }
    }

    private boolean existProject(String repoId,String category){
        JSONObject response=restInterfaceManager.existThisProject(repoId, category);
        return response!=null&&response.getBooleanValue("exist");
    }

    private List<ScanMessageWithTime> getFilteredList(List<ScanMessageWithTime> sourceList){
        int sourceSize=sourceList.size();
        if(sourceSize<=10)
            return sourceList;

        LinkedList<ScanMessageWithTime> result=new LinkedList<>();
        int i=0,step=1;
        while(i<sourceSize){
            if(i>10){
                step+=10;
            }
            result.addFirst(sourceList.get(sourceSize-1-i));
            i+=step;
        }
        return result;
    }

    /**
     * 根据扫描的结果更新project的状态
     *
     * @author WZY
     */
    @Override
    @KafkaListener(id = "scanResult", topics = {"ScanResult"}, groupId = "scanResult")
    public void updateCommitScanStatus(ConsumerRecord<String, String> consumerRecord) {
        String msg = consumerRecord.value();
        logger.info("received message from topic -> " + consumerRecord.topic() + " : " + msg);
        JSONObject projectParam = new JSONObject();
        ScanResult scanResult = JSONObject.parseObject(msg, ScanResult.class);
        String repoId = scanResult.getRepoId();
        String commitId = scanResult.getCommitId();
        String type=scanResult.getType();
        JSONObject commitResponse = restInterfaceManager.getCommitTime(commitId);
        if (commitResponse != null) {
            String commit_time = commitResponse.getJSONObject("data").getString("commit_time");
            projectParam.put("till_commit_time", commit_time);
        }
        projectParam.put("last_scan_time", DateTimeUtil.format(new Date()));
        if (scanResult.getStatus().equals("success")) {
            projectParam.put("scan_status", "Scanned");
        } else {
            if (scanResult.getDescription().equals("Mapping failed")) {
                //mapping 失败，删除当前repo所扫commit得到的rawIssue和location
                restInterfaceManager.deleteRawIssueOfRepo(repoId,type);
            }
            projectParam.put("scan_status", scanResult.getDescription());
        }
        updateProjects(repoId, projectParam,type);
    }
}
