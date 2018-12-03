package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.Location;
import cn.edu.fudan.issueservice.mapper.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@Repository
public class LocationDao {

    private LocationMapper locationMapper;

    @Autowired
    public void setLocationMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    public void insertLocationList(List<Location> list) {
        locationMapper.insertLocationList(list);
    }

    public void deleteLocationByRepoIdAndCategory(String repoId,String category) {
        locationMapper.deleteLocationByRepoIdAndCategory(repoId,category);
    }

    public List<Location> getLocations(String rawIssueId) {
        return locationMapper.getLocations(rawIssueId);
    }
}
