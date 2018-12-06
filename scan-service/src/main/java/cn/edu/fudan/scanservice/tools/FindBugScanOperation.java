package cn.edu.fudan.scanservice.tools;

import cn.edu.fudan.scanservice.domain.Scan;
import cn.edu.fudan.scanservice.domain.ScanInitialInfo;
import cn.edu.fudan.scanservice.domain.ScanResult;
import cn.edu.fudan.scanservice.util.ASTUtil;
import cn.edu.fudan.scanservice.util.ExcuteShellUtil;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("findBug")
public class FindBugScanOperation extends ScanOperationAdapter {

    private final static Logger logger = LoggerFactory.getLogger(FindBugScanOperation.class);

    @Value("${resultFileHome}")
    private String resultFileHome;
    @Value("${repoHome}")
    private String repoHome;

    private ExcuteShellUtil excuteShellUtil;

    @Autowired
    public void setExcuteShellUtil(ExcuteShellUtil excuteShellUtil) {
        this.excuteShellUtil = excuteShellUtil;
    }

    @SuppressWarnings("unchecked")
    private String getJsonString(Element element) {
        List<Attribute> attributes = (List<Attribute>) element.attributes();
        StringBuilder json = new StringBuilder();
        json.append("{");
        for (int i = 0; i < attributes.size(); i++) {
            json.append("\"");
            json.append(attributes.get(i).getName());
            json.append("\":\"");
            json.append(attributes.get(i).getValue());
            json.append("\"");
            if (i != attributes.size() - 1)
                json.append(",");
        }
        json.append("}");
        return json.toString();
    }

    private boolean executeAnalyzeTool(String repoPath, String projectName) {
        return excuteShellUtil.excuteAnalyse(repoPath, projectName);
    }

    @SuppressWarnings("unchecked")
    private String analyzeLocations(String rawIssueUUID, String repoName, Element bugInstance, List<JSONObject> locations) {
        Element sourceLineInClass = bugInstance.element("Class").element("SourceLine");
        String className = sourceLineInClass.attributeValue("classname");
        String fileName = sourceLineInClass.attributeValue("sourcefile");
        String sourcePath=sourceLineInClass.attributeValue("sourcepath");
        String filePath=null;
        String candidateFilePaths = excuteShellUtil.getFileLocation(repoName, fileName);
        if (candidateFilePaths == null) {
            logger.error(sourcePath + " 找不到源文件！");
            return null;
        }else{
            String []candidates=candidateFilePaths.split(":");
            Pattern pattern= Pattern.compile("[/A-Za-z0-9_\\-]*"+sourcePath);
            for(String candidate:candidates){
                Matcher matcher = pattern.matcher(candidate);
                if(matcher.matches()){
                    filePath=candidate;
                    break;
                }
            }
        }
        if(filePath==null){
            logger.error(sourcePath + " 找不到源文件！");
            return null;
        }
        //Method节点
        Element method = bugInstance.element("Method");
        Iterator<Element> iterator = bugInstance.elementIterator("SourceLine");
        if (method == null)
            return null;
        String methodName = method.attributeValue("name");
        Element sourceLineInMethod = method.element("SourceLine");
        int start = Integer.parseInt(sourceLineInMethod.attributeValue("start"));
        int end = Integer.parseInt(sourceLineInMethod.attributeValue("end"));
        String bugLines = null;
        String code = null;
        if (iterator != null) {
            Set<String> container = new HashSet<>();
            StringBuilder bugLineBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                Element SourceLine = iterator.next();
                String startBugLine = SourceLine.attributeValue("start");
                if (!container.contains(startBugLine)) {
                    container.add(startBugLine);
                    bugLineBuilder.append(",");
                    bugLineBuilder.append(startBugLine);
                }
            }
            if (bugLineBuilder.length() > 0)
                bugLines = bugLineBuilder.deleteCharAt(0).toString();
            if (container.size() > 0)
                code = ASTUtil.getCodeAtSpecificLines(container, repoHome + filePath);
            else
                code = ASTUtil.getCode(start, end, repoHome + filePath);
        } else {
            code = ASTUtil.getCode(start, end, repoHome + filePath);
        }
        filePath = filePath.substring(filePath.indexOf("/") + 1);
        JSONObject location = new JSONObject();
        location.put("uuid", UUID.randomUUID().toString());
        location.put("start_line", start);
        location.put("end_line", end);
        location.put("bug_lines", bugLines);
        location.put("file_path", filePath);
        location.put("class_name", className);
        location.put("method_name", methodName);
        location.put("rawIssue_id", rawIssueUUID);
        location.put("code", code);
        locations.add(location);
        return filePath;
    }

    @SuppressWarnings("unchecked")
    private boolean analyzeXML(Scan scan, String repoName, String xmlPath) {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new File(xmlPath));
            Element root = doc.getRootElement();
            Element summary = root.element("FindBugsSummary");
            String result_summary = getJsonString(summary);
            scan.setResult_summary(result_summary);

            Iterator<Element> iterator = root.elementIterator("BugInstance");
            List<JSONObject> rawIssues = new ArrayList<>();
            while (iterator.hasNext()) {
                Element bugInstance = iterator.next();
                List<JSONObject> locations = new ArrayList<>();//每个rawIssue会有多个location
                String rawIssueUUID = UUID.randomUUID().toString();
                //解析当前bugInstance中的location
                String fileName = analyzeLocations(rawIssueUUID, repoName, bugInstance, locations);
                if (fileName != null&&!locations.isEmpty()) {
                    //只有location解析成功并且rawIssue有location才会插入当前rawIssue
                    JSONObject rawIssue = new JSONObject();
                    rawIssue.put("type", bugInstance.attributeValue("type"));
                    rawIssue.put("category","bug");
                    rawIssue.put("detail", getJsonString(bugInstance));
                    rawIssue.put("file_name", fileName);
                    rawIssue.put("scan_id", scan.getUuid());
                    rawIssue.put("commit_id", scan.getCommit_id());
                    rawIssue.put("uuid", rawIssueUUID);
                    rawIssue.put("locations", locations);
                    rawIssues.add(rawIssue);
                }
            }
            if (!rawIssues.isEmpty()) {
                //插入所有的rawIssue
                restInterfaceManager.insertRawIssuesWithLocations(rawIssues);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean compile(String repoPath) {
        return excuteShellUtil.excuteMvn(repoPath);
    }

    @Override
    public ScanResult doScan(ScanInitialInfo scanInitialInfo) {
        Scan scan = scanInitialInfo.getScan();
        String repoPath = scanInitialInfo.getRepoPath();
        String repoName = scanInitialInfo.getRepoName();

        logger.info("start to compile the repository ->" + repoPath);
        if (!compile(repoPath)) {
            logger.error("Project Compile Failed!");
            return new ScanResult("findbug","failed", "compile failed");
        }
        logger.info("compile complete");
        logger.info("start to invoke tool to scan......");
        //如果编译成功,调用findBug得到输出的XML文件
        if (!executeAnalyzeTool(repoPath, repoName)) {
            logger.error("Invoke Analyze Tool Failed!");
            return new ScanResult("findbug","failed", "tool invoke failed");
        }
        logger.info("scan complete");
        logger.info("start to analyze resultFile......");
        //如果fingBug调用成功，开始解析XML文件
        String xmlPath = resultFileHome + repoName + ".xml";
        if (!analyzeXML(scan, repoPath, xmlPath)) {
            logger.error("Result File Analyze Failed!");
            return new ScanResult("findbug","failed", "analyze failed");
        }
        logger.info("resultFile analyze complete");
        return new ScanResult("findbug","success", "Scan Success!");
    }
}
