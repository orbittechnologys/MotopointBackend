package com.ot.moto.dao;


import com.ot.moto.entity.OrgReports;
import com.ot.moto.repository.OrgReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public class OrgReportsDao {

    @Autowired
    private OrgReportsRepository orgReportsRepository;


    public List<OrgReports> saveAll(List<OrgReports> orgReportsList) {
        return orgReportsRepository.saveAll(orgReportsList);

    }
}
