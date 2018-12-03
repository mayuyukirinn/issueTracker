package cn.edu.fudan.scanservice.mapper;

import cn.edu.fudan.scanservice.domain.Scan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanMapper {

    Integer getScanCountByCommitId(String commit_id);

    void insertOneScan(Scan scan);

    void deleteScanByRepoIdAndCategory(@Param("repo_id") String repo_id,@Param("category")String category);

    void updateOneScan(Scan scan);

    String getLatestScannedCommitId(@Param("repo_id") String repo_id,@Param("category")String category);

    List<Scan> getScannedCommits(@Param("repo_id") String repo_id,@Param("category")String category);
}
