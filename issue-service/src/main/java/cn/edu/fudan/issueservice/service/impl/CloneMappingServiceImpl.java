package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.domain.EventType;
import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.domain.RawIssue;
import cn.edu.fudan.issueservice.util.LocationCompare;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Service("cloneMapping")
public class CloneMappingServiceImpl extends BaseMappingServiceImpl {


    //每一次映射完产生的所有的新的clone group
    private void newCloneInsert(boolean isFirst,Map<String,List<RawIssue>> map,Set<String> groupsNeedInsert,String repo_id,String current_commit_id,Date commitDate,String category,String committer,Date date){
        List<Issue> insertIssueList = new ArrayList<>();
        for(String group:groupsNeedInsert){
            //所有新的group，每一个都是一个新的issue
            String new_IssueId=UUID.randomUUID().toString();
            Issue issue = new Issue(new_IssueId, group,category, current_commit_id, commitDate,current_commit_id,commitDate, null,null, repo_id, null,date,date);
            List<RawIssue> rawIssuesInOneGroup=map.get(group);
            for(int i=0;i<rawIssuesInOneGroup.size();i++){
                RawIssue rawIssue=rawIssuesInOneGroup.get(i);
                if(i==0)
                    issue.setRaw_issue_start(rawIssue.getUuid());
                if(i==rawIssuesInOneGroup.size()-1)
                    issue.setRaw_issue_end(rawIssue.getUuid());
                rawIssue.setIssue_id(new_IssueId);
            }
            insertIssueList.add(issue);
        }
        //新的issue
        if (!insertIssueList.isEmpty()) {
            issueDao.insertIssueList(insertIssueList);
            log.info("new issues insert success!");
            issueEventManager.sendIssueEvent(EventType.NEW_CLONE_CLASS,insertIssueList,committer,repo_id);
            newIssueInfoUpdate(insertIssueList,category,repo_id);
            log.info("new issues id saved into redis!");
        }
        if(isFirst){
            int newIssueCount = insertIssueList.size();
            int remainingIssueCount = insertIssueList.size();
            int eliminatedIssueCount = 0;
            log.info("finish mapping -> new:{},remaining:{},eliminated:{}",newIssueCount,remainingIssueCount,eliminatedIssueCount);
            dashboardUpdate(repo_id, newIssueCount, remainingIssueCount, eliminatedIssueCount,category);
            log.info("dashboard info update success!");
        }
    }

    @Override
    public void mapping(String repo_id, String pre_commit_id, String current_commit_id, String category, String committer) {
        Date date=new Date();
        Date commitDate = getCommitDate(current_commit_id);
        if (pre_commit_id.equals(current_commit_id)) {
            List<RawIssue> rawIssues = rawIssueDao.getRawIssueByCommitIDAndCategory(category,current_commit_id);
            if (rawIssues == null || rawIssues.isEmpty())
                return;
            log.info("first scan mapping!");
            Map<String,List<RawIssue>> map=rawIssues.stream().collect(Collectors.groupingBy(RawIssue::getType));
            //对于第一次而言所有的group都是新增的
            newCloneInsert(true,map,map.keySet(),repo_id,current_commit_id,commitDate,category,committer,date);
            rawIssueDao.batchUpdateIssueId(rawIssues);
        }else{
            //不是第一次扫描，需要和前一次的commit进行mapping
            List<RawIssue> rawIssues1 = rawIssueDao.getRawIssueByCommitIDAndCategory(category,pre_commit_id);
            List<RawIssue> rawIssues2 = rawIssueDao.getRawIssueByCommitIDAndCategory(category,current_commit_id);
            if (rawIssues2 == null || rawIssues2.isEmpty())
                return;
            log.info("not first mapping!");
            //装需要更新的Issue
            List<Issue> issues = new ArrayList<>();
            Map<String,List<RawIssue>> map1=rawIssues1.stream().collect(Collectors.groupingBy(RawIssue::getType));
            Map<String,List<RawIssue>> map2=rawIssues2.stream().collect(Collectors.groupingBy(RawIssue::getType));
            Set<String> preGroups=map1.keySet();
            Set<String> currentGroups=map2.keySet();
            Set<String> newGroups=new HashSet<>();
            int equalsCount = 0;
            for(String currentGroup:currentGroups){
                boolean groupMapped=false;
                List<RawIssue> rawIssuesInCurrentGroup=map2.get(currentGroup);
                for(String preGroup:preGroups){
                    if(currentGroup.equals(preGroup)){
                        groupMapped=true;
                        equalsCount++;
                        Issue issue=issueDao.getIssueByID(map1.get(preGroup).get(0).getIssue_id());
                        for(int i=0;i<rawIssuesInCurrentGroup.size();i++){
                            RawIssue rawIssue=rawIssuesInCurrentGroup.get(i);
                            rawIssue.setIssue_id(issue.getUuid());//当前group的所有rawIssue都对应到匹配到的group的issue
                            if(i==rawIssuesInCurrentGroup.size()-1){
                                issue.setEnd_commit(current_commit_id);
                                issue.setEnd_commit_date(commitDate);
                                issue.setRaw_issue_end(rawIssue.getUuid());
                                issue.setUpdate_time(date);
                                issues.add(issue);
                            }
                        }
                        //匹配的2个group内的所有clone instance进行映射
                        cloneInstanceMapping(rawIssuesInCurrentGroup,map1.get(preGroup),committer,repo_id);
                        break;
                    }
                }
                if(!groupMapped){
                    //当前某个clone group没有找到匹配,说明是一个新的clone group,即新的Issue
                    //保存这些新的clone group，之后统一插入
                    newGroups.add(currentGroup);
                }
            }
            //group映射完成,新的group作为新的issue插进去
            newCloneInsert(false,map2,newGroups,repo_id,current_commit_id,commitDate,category,committer,date);

            if (!issues.isEmpty()) {
                //更新issue
                issueDao.batchUpdateIssue(issues);
                log.info("issues update success!");
            }
            int eliminatedIssueCount = preGroups.size() - equalsCount;
            int remainingIssueCount = currentGroups.size();
            int newIssueCount = currentGroups.size() - equalsCount;
            log.info("finish mapping -> new:{},remaining:{},eliminated:{}",newIssueCount,remainingIssueCount,eliminatedIssueCount);
            dashboardUpdate(repo_id, newIssueCount, remainingIssueCount, eliminatedIssueCount,category);
            log.info("dashboard info updated!");
            rawIssueDao.batchUpdateIssueId(rawIssues2);
            addSolvedTag(repo_id, pre_commit_id,EventType.REMOVE_CLONE_CLASS,committer);
        }
        log.info("mapping finished!");
    }

    private void  cloneInstanceMapping(List<RawIssue> pre ,List<RawIssue> current,String committer,String repoId){
        List<RawIssue> removedCloneInstance=new ArrayList<>();
        List<RawIssue> newCloneInstance=new ArrayList<>();
        for(RawIssue currentRawIssue:current){
            boolean isMapped=false;
            for(RawIssue preRawIssue:pre){
                if(!preRawIssue.isMapped()&& LocationCompare.isUniqueIssue(currentRawIssue,preRawIssue)){
                    preRawIssue.setMapped(true);
                    isMapped=true;
                    break;
                }
            }
            if(!isMapped){
                //当前这个group新增的clone instance
                newCloneInstance.add(currentRawIssue);
            }
        }
        for(RawIssue preRawIssue:pre){
            if(!preRawIssue.isMapped()){
                //pre group没匹配上的是消除的clone instance
                removedCloneInstance.add(preRawIssue);
            }
        }
        issueEventManager.sendRawIssueEvent(EventType.NEW_CLONE_INSTANCE,newCloneInstance,committer,repoId);
        issueEventManager.sendRawIssueEvent(EventType.REMOVE_CLONE_INSTANCE,removedCloneInstance,committer,repoId);
    }
}
