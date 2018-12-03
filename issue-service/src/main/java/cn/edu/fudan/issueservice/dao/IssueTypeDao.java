package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.IssueType;
import cn.edu.fudan.issueservice.mapper.IssueTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IssueTypeDao {

    private IssueTypeMapper issueTypeMapper;

    @Autowired
    public void setIssueMapper(IssueTypeMapper issueTypeMapper) {
        this.issueTypeMapper = issueTypeMapper;
    }

    public IssueType getIssueTypeByTypeName(String type) {
        return issueTypeMapper.getIssueTypeByTypeName(type);
    }
}
