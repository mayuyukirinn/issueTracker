package cn.edu.fudan.scanservice.service.impl;

import cn.edu.fudan.scanservice.component.RestInterfaceManager;
import cn.edu.fudan.scanservice.dao.ScanDao;
import cn.edu.fudan.scanservice.domain.Scan;
import cn.edu.fudan.scanservice.domain.ScanInitialInfo;
import cn.edu.fudan.scanservice.domain.ScanResult;
import cn.edu.fudan.scanservice.service.ScanOperation;
import cn.edu.fudan.scanservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;


@Service
public class ScanOperationAdapter implements ScanOperation {

    private final static Logger logger = LoggerFactory.getLogger(ScanOperationAdapter.class);

    protected RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    private ScanDao scanDao;

    @Autowired
    public void setScanDao(ScanDao scanDao) {
        this.scanDao = scanDao;
    }

    @Override
    public boolean isScanned(String commitId) {
        return scanDao.isScanned(commitId);
    }

    @Override
    public boolean checkOut(String repoId, String commitId) {
        JSONObject response = restInterfaceManager.checkOut(repoId, commitId);
        return response != null && response.getJSONObject("data").getString("status").equals("Successful");
    }

    @Override
    public ScanInitialInfo initialScan(String repoId, String commitId,String category) {
        Date startTime = new Date();
        JSONObject currentRepo = restInterfaceManager.getRepoById(repoId);
        String repoName = currentRepo.getJSONObject("data").getString("repo_name");
        String repoPath = currentRepo.getJSONObject("data").getString("local_addr");
        //新建一个Scan对象
        Scan scan = new Scan();
        scan.setCategory(category);
        scan.setName(repoName + "-" + startTime.getTime());
        scan.setStart_time(DateTimeUtil.formatedDate(startTime));
        scan.setStatus("doing...");
        scan.setRepo_id(repoId);
        scan.setCommit_id(commitId);
        //scan.set
        String uuid = UUID.randomUUID().toString();
        scan.setUuid(uuid);
        //use api provided by commit-service
        JSONObject jsonObject = restInterfaceManager.getCommitTime(commitId);
        Date commit_time = jsonObject.getJSONObject("data").getDate("commit_time");
        scan.setCommit_time(DateTimeUtil.formatedDate(commit_time));
        return new ScanInitialInfo(scan, repoName, repoId, repoPath);
    }

    @Override
    public ScanResult doScan(ScanInitialInfo scanInitialInfo) {
        //等待子类的具体实现
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean mapping(String repoId, String commitId,String category) {
        String pre_commit_id = scanDao.getLatestScannedCommitId(repoId,category);
        JSONObject requestParam = new JSONObject();
        requestParam.put("repo_id", repoId);
        requestParam.put("category",category);
        if (pre_commit_id != null)
            requestParam.put("pre_commit_id", pre_commit_id);
        else
            requestParam.put("pre_commit_id", commitId);
        requestParam.put("current_commit_id", commitId);
        logger.info("mapping between " + requestParam.toJSONString());
        JSONObject result = restInterfaceManager.mapping(requestParam);
        return result != null && result.getIntValue("code") == 200;
    }

    @Override
    public boolean updateScan(ScanInitialInfo scanInitialInfo) {
        Scan scan = scanInitialInfo.getScan();
        //更新当前Scan的状态
        scan.setStatus("done");//设为结束状态
        scan.setEnd_time(DateTimeUtil.formatedDate(new Date()));
        scanDao.insertOneScan(scan);
        return true;
    }
}
