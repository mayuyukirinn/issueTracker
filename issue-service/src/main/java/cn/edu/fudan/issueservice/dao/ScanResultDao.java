package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.IssueCountPo;
import cn.edu.fudan.issueservice.domain.ScanResult;
import cn.edu.fudan.issueservice.mapper.ScanResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@Repository
public class ScanResultDao {

    private ScanResultMapper scanResultMapper;

    @Autowired
    public void setScanResultMapper(ScanResultMapper scanResultMapper) {
        this.scanResultMapper = scanResultMapper;
    }

    public void addOneScanResult(ScanResult scanResult){
        scanResultMapper.addOneScanResult(scanResult);
    }

    public List<IssueCountPo> getScanResultsGroupByDay(List<String> repoIds, String category, String start, String end){
        return scanResultMapper.getScanResultsGroupByDay(repoIds, category, start, end);
    }

    public IssueCountPo getMergedScanResult(List<String> repoIds, String category, String start, String end){
        return scanResultMapper.getMergedScanResult(repoIds, category, start, end);
    }
}
