package cn.edu.fudan.cloneservice.task;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.domain.Scan;
import cn.edu.fudan.cloneservice.domain.ScanResult;
import cn.edu.fudan.cloneservice.lock.RedisLuaLock;
import cn.edu.fudan.cloneservice.util.ASTUtil;
import cn.edu.fudan.cloneservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author WZY
 * @version 1.0
 **/
@Component("cloneScanTask")
public class CloneScanTask {

    private static final Logger logger= LoggerFactory.getLogger(CloneScanTask.class);


    @Value("${workHome}")
    private String workHome;
    @Value("${resultFileHome}")
    private String resultFileHome;
    @Value("${repoHome}")
    private String repoHome;
    @Value("${shareDir}")
    private String shareDir;

    private KafkaTemplate kafkaTemplate;
    private RedisLuaLock redisLock;
    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    @Autowired
    public void setRedisLock(RedisLuaLock redisLock) {
        this.redisLock = redisLock;
    }

    @SuppressWarnings("unchecked")
    private boolean analyzeResultFile(String scanId,String commitId,String resultFilePath){
        SAXReader reader = new SAXReader();
        try{
            Document doc = reader.read(new File(resultFilePath));
            Element root = doc.getRootElement();
            Iterator<Element> iterator = root.elementIterator("group");
            List<JSONObject> cloneRawIssues=new ArrayList<>();
            while (iterator.hasNext()){
                Element group=iterator.next();
                String group_id=group.attributeValue("id");
                Iterator<Element> cloneInstances=group.elementIterator("cloneInstance");
                while(cloneInstances.hasNext()){
                    //一个clone instance是一个rawIssue
                    List<JSONObject> cloneLocations=new ArrayList<>();
                    Element cloneInstance=cloneInstances.next();
                    String filePath=cloneInstance.attributeValue("path");
                    String cloneRawIssueId= UUID.randomUUID().toString();
                    JSONObject cloneRawIssue=new JSONObject();
                    cloneRawIssue.put("uuid",cloneRawIssueId);
                    cloneRawIssue.put("type",group_id);
                    cloneRawIssue.put("category","clone");
                    cloneRawIssue.put("detail",null);
                    cloneRawIssue.put("file_name",filePath);
                    cloneRawIssue.put("scan_id",scanId);
                    cloneRawIssue.put("commit_id",commitId);

                    JSONObject cloneLocation=new JSONObject();
                    cloneLocation.put("uuid",UUID.randomUUID().toString());
                    int startLine=Integer.parseInt(cloneInstance.attributeValue("startLine"));
                    int endLine=Integer.parseInt(cloneInstance.attributeValue("endLine"));
                    cloneLocation.put("start_line",startLine);
                    cloneLocation.put("end_line",endLine);
                    cloneLocation.put("start_token",Integer.parseInt(cloneInstance.attributeValue("startToken")));
                    cloneLocation.put("end_token",Integer.parseInt(cloneInstance.attributeValue("endToken")));
                    cloneLocation.put("file_path",filePath);
                    cloneLocation.put("rawIssue_id",cloneRawIssueId);
                    cloneLocation.put("code",ASTUtil.getCode(startLine,endLine,repoHome+filePath));
                    cloneLocations.add(cloneLocation);

                    cloneRawIssue.put("locations",cloneLocations);
                    cloneRawIssues.add(cloneRawIssue);
                }
            }
            if(!cloneRawIssues.isEmpty()){
                //插入所有的rawIssue
                restInterfaceManager.insertRawIssuesWithLocations(cloneRawIssues);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean invokeCloneTool(String repoPath,String repoName){
        String cmd = "java -jar SAGA.jar  " + repoPath + " " + repoName;
        BufferedInputStream br = null;
        try {
            Process process = Runtime.getRuntime().exec(cmd,null,new File(workHome));
            //输出process打印信息
            br = new BufferedInputStream(process.getInputStream());
            int ch;
            StringBuilder text = new StringBuilder("getInfo: \n");
            while ((ch = br.read()) != -1) {
                text.append((char) ch);
            }
            logger.info(text.toString());
            process.waitFor();
            return process.exitValue()==0;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean mapping(String repo_id,String current_commit_id,String category){
        String pre_commit_id = restInterfaceManager.getLastScannedCommitId(repo_id,category);
        JSONObject requestParam = new JSONObject();
        requestParam.put("repo_id", repo_id);
        if (pre_commit_id != null)
            requestParam.put("pre_commit_id", pre_commit_id);
        else
            requestParam.put("pre_commit_id", current_commit_id);
        requestParam.put("current_commit_id", current_commit_id);
        requestParam.put("category",category);
        logger.info("mapping between " + requestParam.toJSONString());
        JSONObject result = restInterfaceManager.mapping(requestParam);
        return result != null && result.getIntValue("code") == 200;
    }

    private boolean checkOut(String repoId, String commitId) {
        JSONObject response = restInterfaceManager.checkOut(repoId, commitId);
        return response != null && response.getJSONObject("data").getString("status").equals("Successful");
    }

    private void startScan(String repoId,String repoName,String repoPath,Scan scan){
        logger.info(repoPath+"  : "+repoName);
        String scanId=scan.getUuid();
        String commitId=scan.getCommit_id();
        logger.info("start to checkout -> " + commitId);
        //checkout,如果失败发送错误消息，直接返回
        if (!checkOut(repoId, commitId)) {
            send(repoId, commitId, "failed", "check out failed");
            logger.error("Check Out Failed!");
            return;
        }
        logger.info("checkout complete -> start the scan operation......");
        logger.info("start to invoke clone tool to scan......");
        if(!invokeCloneTool(repoPath, repoName)){
            send(repoId,commitId,"failed","tool invoke failed!");
            logger.error("tool invoke failed!");
            return;
        }
        logger.info("tool invoke complete!");
        logger.info("start to analyze result file");
        String resultFilePath1=resultFileHome+repoName+"_filtedA_type12.csv.xml";
        String resultFilePath2=resultFileHome+repoName+"_filtedB_merge.csv.xml";
        if(!analyzeResultFile(scanId,commitId,resultFilePath1)||!analyzeResultFile(scanId,commitId,resultFilePath2)){
            send(repoId,commitId,"failed","file analyze failed!");
            logger.error("file analyze failed!");
            return;
        }
        logger.info("file analyze success!");
        logger.info("start to mapping......");
        if(!mapping(repoId,commitId,scan.getCategory())){
            send(repoId,commitId,"failed","mapping failed!");
            logger.error("mapping failed!");
            return;
        }
        logger.info("mapping success!");
        logger.info("start to insert scan.....");
        scan.setStatus("done");//设为结束状态
        scan.setEnd_time(DateTimeUtil.formatedDate(new Date()));
        JSONObject response =restInterfaceManager.insertScan(scan);
        if(response==null||response.getIntValue("code")!=200){
            send(repoId,commitId,"failed","scan add failed!");
            logger.error("scan add failed!");
            return;
        }
        send(repoId,commitId,"success","scan success");
        logger.info("scan complete!");
    }

    @Async
    public Future<String> run(String repoId, String repoName, String repoPath, Scan scan) {
        //获取分布式锁，一个repo同一时间只能有一个线程在扫
        //15min恰好是一个整个Scan操作的超时时间，如果某个线程获得锁之后Scan过程卡死导致锁没有释放
        //如果那个锁成功设置了过期时间，那么key过期后，其他线程自然可以获取到锁
        //如果那个锁并没有成功地设置过期时间
        //那么等待获取同一个锁的线程会因为10min的超时而强行获取到锁，并设置自己的identifier和key的过期时间
        String identifier = redisLock.acquireLockWithTimeOut(repoId, 10, 10, TimeUnit.MINUTES);
        logger.info("repo->" + repoId + "get the lock :"+identifier);
        try {
            startScan(repoId, repoName, shareDir+repoPath, scan);
        } finally {
            if (redisLock.releaseLock(repoId, identifier)) {
                logger.error("repo->" + repoId + " release lock success!");
            }
        }
        return new AsyncResult<>("complete");

    }

    @SuppressWarnings("unchecked")
    private void send(String repoId, String commitId ,String status, String description) {
        ScanResult scanResult = new ScanResult(repoId, commitId,"clone" ,status, description);
        kafkaTemplate.send("ScanResult", JSONObject.toJSONString(scanResult));
    }

}
