package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.IssueCountPo;
import cn.edu.fudan.issueservice.domain.ScanResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanResultMapper {

    void addOneScanResult(ScanResult scanResult);

    List<IssueCountPo> getScanResultsGroupByDay(@Param("list") List<String> repoIds, @Param("category") String category, @Param("start") String start, @Param("end") String end);

    IssueCountPo getMergedScanResult(@Param("list") List<String> repoIds, @Param("category") String category, @Param("start") String start, @Param("end") String end);
}
