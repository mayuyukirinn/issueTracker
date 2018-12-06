package cn.edu.fudan.scanservice.tools;

import cn.edu.fudan.scanservice.domain.Scan;
import cn.edu.fudan.scanservice.domain.ScanInitialInfo;
import cn.edu.fudan.scanservice.domain.ScanResult;
import cn.edu.fudan.scanservice.util.ASTUtil;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author WZY
 * @version 1.0
 **/
@Component("CPUClone")
public class CPUCloneScanOperation extends ScanOperationAdapter {

    private static final Logger logger= LoggerFactory.getLogger(CPUCloneScanOperation.class);

    @Value("${workHome}")
    private String workHome;
    @Value("${resultFileHome}")
    private String resultFileHome;
    @Value("${repoHome}")
    private String repoHome;

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
                    cloneLocation.put("code", ASTUtil.getCode(startLine,endLine,repoHome+filePath));
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

    @Override
    public ScanResult doScan(ScanInitialInfo scanInitialInfo) {
        Scan scan = scanInitialInfo.getScan();
        String scanId=scan.getUuid();
        String commitId=scan.getCommit_id();
        String repoId=scanInitialInfo.getRepoId();
        String repoPath = scanInitialInfo.getRepoPath();
        String repoName = scanInitialInfo.getRepoName();
        logger.info("start to invoke tool to scan......");
        if (!invokeCloneTool(repoPath, repoName)) {
            logger.error("Invoke Analyze Tool Failed!");
            return new ScanResult("clone","failed", "tool invoke failed");
        }
        logger.info("scan complete");
        logger.info("start to analyze resultFile......");
        logger.info("tool invoke complete!");
        String resultFilePath1=resultFileHome+repoName+"_A.xml";
        if(!analyzeResultFile(scanId,commitId,resultFilePath1)){
            logger.error("Result File Analyze Failed!");
            return new ScanResult("clone","failed", "analyze failed");
        }
        logger.info("resultFile analyze complete");
        return new ScanResult("clone","success", "Scan Success!");
    }
}
