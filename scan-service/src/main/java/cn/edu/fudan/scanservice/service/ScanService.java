package cn.edu.fudan.scanservice.service;


import cn.edu.fudan.scanservice.domain.Scan;
import com.alibaba.fastjson.JSONArray;

public interface ScanService {

    void insertOneScan(Scan scan);

    void deleteScanByRepoIdAndCategory(String repoId,String category);

    void updateOneScan(Scan scan);

    Object getCommits(String project_id, Integer page, Integer size, Boolean is_whole,String category);

    Object getScannedCommits(String repo_id,String category);

    String getLatestScannedCommitId(String repo_id,String category);
}
