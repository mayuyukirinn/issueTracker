package cn.edu.fudan.issueservice.service;

public interface MappingService {

    void mapping(String repo_id, String pre_commit_id, String current_commit_id,String category,String committer);
}
