package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.ScanResultDao;
import cn.edu.fudan.issueservice.domain.*;
import cn.edu.fudan.issueservice.scheduler.QuartzScheduler;
import cn.edu.fudan.issueservice.service.IssueService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Service
public class IssueServiceImpl implements IssueService {

    @Value("${solved.tag_id}")
    private String solvedTagId;
    @Value("${ignore.tag_id}")
    private String ignoreTagId;

    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    private CloneMappingServiceImpl cloneMappingService;

    @Autowired
    public void setCloneMappingService(CloneMappingServiceImpl cloneMappingService) {
        this.cloneMappingService = cloneMappingService;
    }

    private BugMappingServiceImpl bugMappingService;

    @Autowired
    public void setBugMappingService(BugMappingServiceImpl bugMappingService) {
        this.bugMappingService = bugMappingService;
    }

    private QuartzScheduler quartzScheduler;

    @Autowired
    public void setQuartzScheduler(QuartzScheduler quartzScheduler) {
        this.quartzScheduler = quartzScheduler;
    }

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private IssueDao issueDao;

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    private ScanResultDao scanResultDao;

    @Autowired
    public void setScanResultDao(ScanResultDao scanResultDao) {
        this.scanResultDao = scanResultDao;
    }


    @Override
    public void insertIssueList(List<Issue> list) {
        issueDao.insertIssueList(list);
    }

    @Override
    public void deleteIssueByRepoIdAndCategory(String repoId,String category) {
        log.info("start to delete issue -> repoId={} , category={}",repoId,category);
        //先删除该项目所有issue对应的tag
        List<String> issueIds = issueDao.getIssueIdsByRepoIdAndCategory(repoId, category);
        restInterfaceManager.deleteTagsOfIssueInOneRepo(issueIds);
        log.info("tag delete success!");
        issueDao.deleteIssueByRepoIdAndCategory(repoId,category);
        log.info("issue delete success!");
    }

    @Override
    public void batchUpdateIssue(List<Issue> list) {
        issueDao.batchUpdateIssue(list);
    }

    @Override
    public Issue getIssueByID(String uuid) {
        return issueDao.getIssueByID(uuid);
    }

    private void addTagInfo(List<Issue> issues) {
        for (Issue issue : issues) {
            JSONArray tags = restInterfaceManager.getTagsOfIssue(issue.getUuid());
            issue.setTags(tags);
        }
    }

    @Override
    public Object getIssueList(String project_id, Integer page, Integer size,String category) {
        String repoId=restInterfaceManager.getRepoIdOfProject(project_id);
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("repo_id", repoId);
        param.put("size", size);
        param.put("category",category);
        //获取已经solve的和ignore的issue_ids
        List<String> tag_ids = new ArrayList<>();
        tag_ids.add(solvedTagId);
        tag_ids.add(ignoreTagId);
        JSONArray solved_issue_ids = restInterfaceManager.getSolvedIssueIds(tag_ids);
        if (solved_issue_ids != null && solved_issue_ids.size() > 0) {
            param.put("solved_issue_ids", solved_issue_ids.toJavaList(String.class));
        }
        int count = issueDao.getIssueCount(param);
        param.put("start", (page - 1) * size);
        result.put("totalPage", count % size == 0 ? count / size : count / size + 1);
        result.put("totalCount", count);
        List<Issue> issues = issueDao.getIssueList(param);
        addTagInfo(issues);
        result.put("issueList", issues);
        return result;
    }

    @Override
    public Object getFilteredIssueList(JSONObject requestParam) {
        String project_id = requestParam.getString("project_id");
        int size = requestParam.getIntValue("size");
        int page = requestParam.getIntValue("page");
        String category=requestParam.getString("category");
        if (project_id == null || size == 0 || page == 0)
            throw new IllegalArgumentException("param lost!");
        String repoId = restInterfaceManager.getRepoIdOfProject(project_id);
        JSONArray tag_ids = requestParam.getJSONArray("tags");
        JSONArray types = requestParam.getJSONArray("types");
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> query = new HashMap<>();
        if(tag_ids!=null&&!tag_ids.isEmpty()){
            //只有在筛选条件都不为null的条件下才启用过滤
            JSONArray issue_ids = restInterfaceManager.getSpecificTaggedIssueIds(tag_ids);
            if (issue_ids == null || issue_ids.size() == 0) {//没找到打了这些tag的issue
                result.put("totalPage", 0);
                result.put("totalCount", 0);
                result.put("issueList", Collections.emptyList());
                return result;
            }
            query.put("issue_ids", issue_ids.toJavaList(String.class));
        }
        if(types!=null&&!types.isEmpty()){
            query.put("types", types.toJavaList(String.class));
        }
        query.put("repo_id", repoId);
        query.put("category",category);
        int count = issueDao.getIssueCount(query);
        query.put("size", size);
        query.put("start", (page - 1) * size);
        List<Issue> issues = issueDao.getIssueList(query);
        addTagInfo(issues);

        result.put("totalPage", count % size == 0 ? count / size : count / size + 1);
        result.put("totalCount", count);
        result.put("issueList", issues);
        return result;
    }

    @Override
    public Object getSpecificIssues(IssueParam issueParam) {
        int size=issueParam.getSize();
        int page=issueParam.getPage();
        List<String> issueIds=issueParam.getIssueIds();
        Map<String, Object> result = new HashMap<>();
        if(issueIds==null||issueIds.isEmpty()){
            result.put("totalPage", 0);
            result.put("totalCount", 0);
            result.put("issueList", Collections.emptyList());
            return result;
        }

        Map<String, Object> query = new HashMap<>();
        query.put("list",issueIds);
        query.put("category",issueParam.getCategory());
        int count=issueDao.getSpecificIssueCount(query);
        query.put("size", size);
        query.put("start", (page - 1) * size);
        List<Issue> issues = issueDao.getSpecificIssues(query);
        result.put("totalPage", count % size == 0 ? count / size : count / size + 1);
        result.put("totalCount", count);
        result.put("issueList", issues);
        return result;
    }




    private IssueCount getOneRepoDashBoardInfo(String duration,String repoId,String category){
        Object newObject=stringRedisTemplate.opsForHash().get("dashboard:"+category+":"+ duration + ":" + repoId, "new");
        int newCount = newObject==null?0:Integer.parseInt((String)newObject);
        Object remainingObject=stringRedisTemplate.opsForHash().get("dashboard:"+category+":" + duration + ":" + repoId, "remaining");
        int remainingCount = remainingObject==null?0:Integer.parseInt((String)remainingObject);
        Object eliminatedObject=stringRedisTemplate.opsForHash().get("dashboard:"+category+":" + duration + ":" + repoId, "eliminated");
        int eliminatedCount = eliminatedObject==null?0:Integer.parseInt((String)eliminatedObject);
        List<String> newIssueIds=stringRedisTemplate.opsForList().range("dashboard:"+category+":"+duration+":new"+ repoId,0,-1);
        return new IssueCount(newCount,eliminatedCount,remainingCount,newIssueIds);
    }

    @Override
    public Object getDashBoardInfo(String duration, String project_id, String userToken,String category) {
        IssueCount result=new IssueCount(0,0,0);
        String account_id = restInterfaceManager.getAccountId(userToken);
        if (project_id == null) {
            //未选择某一个project,显示该用户所有project的dashboard信息
            JSONArray repoIds = restInterfaceManager.getRepoIdsOfAccount(account_id,category);
            if (repoIds != null&&!repoIds.isEmpty()) {
                for (int i = 0; i < repoIds.size(); i++) {
                    String currentRepoId = repoIds.getString(i);
                    result.issueCountUpdate(getOneRepoDashBoardInfo(duration,currentRepoId,category));
                }
            }
        } else {
            //只显示当前所选project的相关dashboard信息
            String currentRepoId = restInterfaceManager.getRepoIdOfProject(project_id);
            result.issueCountUpdate(getOneRepoDashBoardInfo(duration,currentRepoId,category));
        }
        return result;
    }

    private List<IssueCount> getFormatData(List<String> newList, List<String> remainingList, List<String> eliminatedList) {
        if (newList == null || remainingList == null || eliminatedList == null)
            return Collections.emptyList();
        List<IssueCount> list = new ArrayList<>();
        for (int i = 0; i < newList.size(); i++) {
            String[] str1=newList.get(i).split(":");
            String[] str2=eliminatedList.get(i).split(":");
            String[] str3=remainingList.get(i).split(":");
            list.add(new IssueCount(str1[0],Integer.parseInt(str1[1]),Integer.parseInt(str2[1]),Integer.parseInt(str3[1])));
        }
        return list;
    }

    @Override
    public Object getStatisticalResults(Integer month, String project_id, String userToken,String category) {
        Map<String, Object> result = new HashMap<>();
        String account_id = restInterfaceManager.getAccountId(userToken);
        String newKey;
        String remainingKey;
        String eliminatedKey;
        if (project_id == null) {
            if (month == 1) {
                newKey = "trend:"+category+":day:new:" + account_id;
                remainingKey = "trend:"+category+":day:remaining:" + account_id;
                eliminatedKey = "trend:"+category+":day:eliminated:" + account_id;
            } else {
                newKey = "trend:"+category+":week:new:" + account_id;
                remainingKey = "trend:"+category+":week:remaining:" + account_id;
                eliminatedKey = "trend:"+category+":week:eliminated:" + account_id;
            }
        } else {
            String repoId = restInterfaceManager.getRepoIdOfProject(project_id);
            if (month == 1) {
                newKey = "trend:"+category+":day:new:" + account_id + ":" + repoId;
                remainingKey = "trend:"+category+":day:remaining:" + account_id + ":" + repoId;
                eliminatedKey = "trend:"+category+":day:eliminated:" + account_id + ":" + repoId;
            } else {
                newKey = "trend:"+category+":week:new:" + account_id + ":" + repoId;
                remainingKey = "trend:"+category+":week:remaining:" + account_id + ":" + repoId;
                eliminatedKey = "trend:"+category+":week:eliminated:" + account_id + ":" + repoId;
            }
        }
        List<String> newList = stringRedisTemplate.opsForList().range(newKey, 0, -1);
        List<String> remainingList = stringRedisTemplate.opsForList().range(remainingKey, 0, -1);
        List<String> eliminatedList = stringRedisTemplate.opsForList().range(eliminatedKey, 0, -1);
        result.put("data", getFormatData(newList, remainingList, eliminatedList));
        return result;
    }

    @Override
    public Object getExistIssueTypes(String category) {
        List<String> types=issueDao.getExistIssueTypes(category);
        if(category.equals("clone")){
            types.sort(Comparator.comparingInt(Integer::valueOf));
        }
        return types;
    }

    @Override
    public Object getAliveAndEliminatedInfo(String project_id, String category) {
        String repoId=restInterfaceManager.getRepoIdOfProject(project_id);
        JSONArray solvedIssueIds=restInterfaceManager.getSpecificTaggedIssueIds(solvedTagId);
        double avgEliminatedTime=0.00;
        long maxAliveTime=0;
        if(solvedIssueIds!=null&&!solvedIssueIds.isEmpty()){
            List<String> solvedIssueIdList=solvedIssueIds.toJavaList(String.class);
            Double avgEliminatedTimeObject=issueDao.getAvgEliminatedTime(solvedIssueIdList,repoId,category);
            avgEliminatedTime=avgEliminatedTimeObject==null?0.00:avgEliminatedTimeObject;
            maxAliveTime=issueDao.getMaxAliveTime(solvedIssueIdList,repoId,category);
        }else{
            //所有issue都是存活的
            maxAliveTime=issueDao.getMaxAliveTime(null,repoId,category);
        }
        return new IssueStatisticInfo(avgEliminatedTime/3600/24,maxAliveTime/3600/24);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startMapping(String repo_id, String pre_commit_id, String current_commit_id,String category) {
        String committer="";
        JSONObject commitInfo=restInterfaceManager.getOneCommitByCommitId(current_commit_id);
        if(commitInfo!=null){
            committer=commitInfo.getJSONArray("data").getJSONObject(0).getString("developer");
        }
        if(category.equals("bug")){
            log.info("start bug mapping -> repo_id={},pre_commit_id={},current_commit_id={}",repo_id,pre_commit_id,current_commit_id);
            bugMappingService.mapping(repo_id,pre_commit_id,current_commit_id,category,committer);
        }else if(category.equals("clone")){
            log.info("start clone mapping -> repo_id={},pre_commit_id={},current_commit_id={}",repo_id,pre_commit_id,current_commit_id);
            cloneMappingService.mapping(repo_id,pre_commit_id,current_commit_id,category,committer);
        }
    }

    @Override
    public void updateIssueCount(String time) {
        quartzScheduler.updateIssueCount(time);
    }

    @Override
    public Object getNewTrend(Integer month, String project_id, String userToken, String category) {
        List<IssueCountPo> result=new ArrayList<>();
        String account_id = restInterfaceManager.getAccountId(userToken);
        LocalDate end=LocalDate.now();
        if(project_id==null||project_id.equals("")){
            //需要查询该用户所有项目的扫描情况
            JSONArray repoIds=restInterfaceManager.getRepoIdsOfAccount(account_id,category);
            if(repoIds!=null){
                if(month==1){
                    //过去30天
                    LocalDate start=end.minusMonths(1);
                    return scanResultDao.getScanResultsGroupByDay(repoIds.toJavaList(String.class),category, DateTimeUtil.y_m_d_format(start),DateTimeUtil.y_m_d_format(end));
                }else if(month==3||month==6){
                    //过去3个月
                    LocalDate start=end.minusMonths(month);
                    while(start.isBefore(end)){
                        LocalDate temp=start.plusWeeks(1);
                        IssueCountPo issueCountPo=scanResultDao.getMergedScanResult(repoIds.toJavaList(String.class),category, DateTimeUtil.y_m_d_format(start),DateTimeUtil.y_m_d_format(temp));
                        if(issueCountPo!=null)
                            result.add(issueCountPo);
                        start=temp;
                    }
                }else{
                    throw new IllegalArgumentException("month should be 1 or 3 or 6");
                }
            }
        }else{
            //只需要查询该项目的扫描情况
            String repoId=restInterfaceManager.getRepoIdOfProject(project_id);
            if(month==1){
                //过去30天
                LocalDate start=end.minusMonths(1);
                return scanResultDao.getScanResultsGroupByDay(Collections.singletonList(repoId),category, DateTimeUtil.y_m_d_format(start),DateTimeUtil.y_m_d_format(end));
            }else if(month==3||month==6){
                //过去3个月
                LocalDate start=end.minusMonths(month);
                while(start.isBefore(end)){
                    LocalDate temp=start.plusWeeks(1);
                    IssueCountPo issueCountPo=scanResultDao.getMergedScanResult(Collections.singletonList(repoId),category, DateTimeUtil.y_m_d_format(start),DateTimeUtil.y_m_d_format(temp));
                    if(issueCountPo!=null)
                        result.add(issueCountPo);
                    start=temp;
                }
            }else{
                throw new IllegalArgumentException("month should be 1 or 3 or 6");
            }
        }
        return result;
    }
}
