package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.LocationDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.domain.Location;
import cn.edu.fudan.issueservice.domain.RawIssue;
import cn.edu.fudan.issueservice.service.RawIssueService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
@Service
public class RawIssueServiceImpl implements RawIssueService {

    @Value("${repoHome}")
    private String repoHome;

    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    private RawIssueDao rawIssueDao;

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    private LocationDao locationDao;

    @Autowired
    public void setLocationDao(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    /**
     * 插入rawIssue列表，同时会插入里面包含的locations
     *
     * @param list rawIssue的列表，里面包含了locations
     * @author WZY
     */
    @Override
    public void insertRawIssueList(List<RawIssue> list) {
        List<Location> locations = new ArrayList<>();
        for (RawIssue rawIssue : list) {
            locations.addAll(rawIssue.getLocations());
        }
        rawIssueDao.insertRawIssueList(list);
        locationDao.insertLocationList(locations);
    }

    /**
     * 删除rawIssue以及对应的locations
     *
     * @param repoId 项目的id
     * @author WZY
     */
    @Transactional
    @Override
    public void deleteRawIssueByRepoIdAndCategory(String repoId,String category) {
        locationDao.deleteLocationByRepoIdAndCategory(repoId,category);
        rawIssueDao.deleteRawIssueByRepoIdAndCategory(repoId,category);
    }

    @Override
    public void batchUpdateIssueId(List<RawIssue> list) {
        rawIssueDao.batchUpdateIssueId(list);
    }

    @Override
    public List<RawIssue> getRawIssueByCommitIDAndCategory(String commit_id,String category) {
        return rawIssueDao.getRawIssueByCommitIDAndCategory(commit_id,category);
    }

    @Override
    public List<RawIssue> getRawIssueByIssueId(String issueId) {
        return rawIssueDao.getRawIssueByIssueId(issueId);
    }

    @Override
    public Object getCode(String project_id, String commit_id, String file_path) {
        file_path=file_path.replaceAll("\\\\","/");
        Map<String, Object> result = new HashMap<>();
        String repo_id =restInterfaceManager.getRepoIdOfProject(project_id) ;
        JSONObject response = restInterfaceManager.checkOut(repo_id,commit_id);
        if (response != null && response.getJSONObject("data").getString("status").equals("Successful")) {
            JSONObject codeResponse = restInterfaceManager.getCode(repoHome+file_path);
            if (codeResponse != null && codeResponse.getJSONObject("data").getString("status").equals("Successful")) {
                result.put("code", codeResponse.getJSONObject("data").getString("content"));
            } else {
                throw new RuntimeException("load file failed!");
            }
        } else {
            throw new RuntimeException("check out failed!");
        }
        return result;
    }

    @Override
    public List<Location> getLocationsByRawIssueId(String raw_issue_id) {
        return locationDao.getLocations(raw_issue_id);
    }
}
