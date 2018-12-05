package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.TagMapHelper;
import cn.edu.fudan.issueservice.domain.*;
import cn.edu.fudan.issueservice.util.LocationCompare;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Service("bugMapping")
public class BugMappingServiceImpl extends BaseMappingServiceImpl {


    private TagMapHelper tagMapHelper;

    @Autowired
    public void setTagMapHelper(TagMapHelper tagMapHelper) {
        this.tagMapHelper = tagMapHelper;
    }

    private void addTag(List<JSONObject> tags,RawIssue rawIssue,String issueId){
        RawIssueDetail rawIssueDetail= JSONObject.parseObject(rawIssue.getDetail(),RawIssueDetail.class);
        String tagID=tagMapHelper.getTagIdByRank(Integer.parseInt(rawIssueDetail.getRank()));
        if(tagID!=null){
            JSONObject tagged = new JSONObject();
            tagged.put("item_id", issueId);
            tagged.put("tag_id", tagID);
            tags.add(tagged);
        }
    }



    @Override
    public void mapping(String repo_id, String pre_commit_id, String current_commit_id, String category, String committer) {
        List<Issue> insertIssueList = new ArrayList<>();
        List<JSONObject> tags = new ArrayList<>();
        Date date= new Date();
        if (pre_commit_id.equals(current_commit_id)) {
            //当前project第一次扫描，所有的rawIssue都是issue
            List<RawIssue> rawIssues = rawIssueDao.getRawIssueByCommitIDAndCategory(category,current_commit_id);
            if (rawIssues == null || rawIssues.isEmpty())
                return;
            log.info("first scan mapping!");
            Date commitDate=getCommitDate(current_commit_id);
            for (RawIssue rawIssue : rawIssues) {
                String new_IssueId = UUID.randomUUID().toString();
                rawIssue.setIssue_id(new_IssueId);
                String targetFiles = rawIssue.getFile_name();
                Issue issue = new Issue(new_IssueId, rawIssue.getType(),category, current_commit_id,commitDate, current_commit_id,commitDate, rawIssue.getUuid(), rawIssue.getUuid(), repo_id, targetFiles,date,date);
                insertIssueList.add(issue);
                addTag(tags,rawIssue,new_IssueId);
            }
            int newIssueCount = insertIssueList.size();
            int remainingIssueCount = insertIssueList.size();
            int eliminatedIssueCount = 0;
            log.info("finish mapping -> new:{},remaining:{},eliminated:{}",newIssueCount,remainingIssueCount,eliminatedIssueCount);
            dashboardUpdate(repo_id, newIssueCount, remainingIssueCount, eliminatedIssueCount,category);
            log.info("dashboard info updated!");
            rawIssueDao.batchUpdateIssueId(rawIssues);
            scanResultDao.addOneScanResult(new ScanResult(category,repo_id,date,commitDate,newIssueCount,eliminatedIssueCount,remainingIssueCount));
        } else {
            //不是第一次扫描，需要和前一次的commit进行mapping
            List<RawIssue> rawIssues1 = rawIssueDao.getRawIssueByCommitIDAndCategory(category,pre_commit_id);
            List<RawIssue> rawIssues2 = rawIssueDao.getRawIssueByCommitIDAndCategory(category,current_commit_id);
            if (rawIssues2 == null || rawIssues2.isEmpty())
                return;
            log.info("not first mapping!");
            Date commitDate = getCommitDate(current_commit_id);
            //装需要更新的
            List<Issue> issues = new ArrayList<>();
            int equalsCount = 0;
            for (RawIssue issue_2 : rawIssues2) {
                boolean mapped = false;
                for (RawIssue issue_1 : rawIssues1) {
                    //如果issue_1已经匹配到一个issue_2,内部循环不再继续
                    if (!issue_1.isMapped()&&!issue_2.isMapped()&& LocationCompare.isUniqueIssue(issue_1, issue_2)) {
                        issue_1.setMapped(true);
                        issue_2.setMapped(true);
                        mapped = true;
                        equalsCount++;
                        String pre_issue_id = issue_1.getIssue_id();
                        //如果匹配到的上个commit的某个rawIssue已经有了issue_id,说明当前commit这个rawIssue也应该对应到这个issue
                        issue_2.setIssue_id(pre_issue_id);
                        Issue issue = issueDao.getIssueByID(pre_issue_id);
                        issue.setEnd_commit(current_commit_id);
                        issue.setEnd_commit_date(commitDate);
                        issue.setRaw_issue_end(issue_2.getUuid());
                        issue.setUpdate_time(new Date());
                        issues.add(issue);
                        break;
                    }
                }
                if (!mapped) {
                    //如果当前commit的某个rawIssue没有在上个commit的rawissue列表里面找到匹配，将它作为新的issue插入
                    String new_IssueId = UUID.randomUUID().toString();
                    issue_2.setIssue_id(new_IssueId);
                    String targetFiles = issue_2.getFile_name();
                    insertIssueList.add(new Issue(new_IssueId, issue_2.getType(),category, current_commit_id, commitDate,current_commit_id,commitDate, issue_2.getUuid(), issue_2.getUuid(), repo_id, targetFiles,date,date));
                    addTag(tags,issue_2,new_IssueId);
                }
            }
            if (!issues.isEmpty()) {
                //更新issue
                issueDao.batchUpdateIssue(issues);
                log.info("issue update success!");
            }
            int eliminatedIssueCount = rawIssues1.size() - equalsCount;
            int remainingIssueCount = rawIssues2.size();
            int newIssueCount = rawIssues2.size() - equalsCount;
            log.info("finish mapping -> new:{},remaining:{},eliminated:{}",newIssueCount,remainingIssueCount,eliminatedIssueCount);
            dashboardUpdate(repo_id, newIssueCount, remainingIssueCount, eliminatedIssueCount,category);
            log.info("dashboard info updated!");
            rawIssueDao.batchUpdateIssueId(rawIssues2);
            modifyToSolvedTag(repo_id, pre_commit_id,EventType.ELIMINATE_BUG,committer);
            scanResultDao.addOneScanResult(new ScanResult(category,repo_id,date,commitDate,newIssueCount,eliminatedIssueCount,remainingIssueCount));
        }
        //新的issue
        if (!insertIssueList.isEmpty()) {
            issueDao.insertIssueList(insertIssueList);
            issueEventManager.sendIssueEvent(EventType.NEW_BUG,insertIssueList,committer,repo_id);
            newIssueInfoUpdate(insertIssueList,category,repo_id);
            log.info("new issue insert success!");
        }
        //打tag
        if(!tags.isEmpty()){
            restInterfaceManager.addTags(tags);
        }
        log.info("mapping finished!");
    }
}
