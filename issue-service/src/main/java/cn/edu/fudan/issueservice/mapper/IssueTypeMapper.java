package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.IssueType;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueTypeMapper {

    IssueType getIssueTypeByTypeName(@Param("type") String type);
}
