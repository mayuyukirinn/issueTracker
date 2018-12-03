package cn.edu.fudan.scanservice.dao;

import cn.edu.fudan.scanservice.domain.Scan;
import cn.edu.fudan.scanservice.mapper.ScanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ScanDao {


    private ScanMapper scanMapper;

    @Autowired
    public void setScanMapper(ScanMapper scanMapper) {
        this.scanMapper = scanMapper;
    }

    public void insertOneScan(Scan scan) {
        scanMapper.insertOneScan(scan);
    }

    public void deleteScanByRepoIdAndCategory(String repoId,String category) {
        scanMapper.deleteScanByRepoIdAndCategory(repoId,category);
    }

    public void updateOneScan(Scan scan) {
        scanMapper.updateOneScan(scan);
    }

    public String getLatestScannedCommitId(String repo_id,String category) {
        return scanMapper.getLatestScannedCommitId(repo_id,category);
    }

    public boolean isScanned(String commit_id) {
        Integer count = scanMapper.getScanCountByCommitId(commit_id);
        return count != null && count > 0;
    }

    public List<String> getScannedCommits(String repo_id,String category) {
        List<Scan> scans = scanMapper.getScannedCommits(repo_id,category);
        List<String> list = new ArrayList<>();
        for (Scan scan : scans) {
            list.add(scan.getCommit_id());
        }
        return list;
    }

    public List<Scan> getScans(String repo_id,String category) {
        return scanMapper.getScannedCommits(repo_id,category);
    }
}
