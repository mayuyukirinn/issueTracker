package cn.edu.fudan.issueservice.domain;

import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
public class IssueParam {

    private String category;
    private int page;
    private int size;
    private List<String> issueIds;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String> getIssueIds() {
        return issueIds;
    }

    public void setIssueIds(List<String> issueIds) {
        this.issueIds = issueIds;
    }
}
