package com.ot.moto.dao;


import com.ot.moto.entity.OrgReports;
import com.ot.moto.entity.Tam;
import com.ot.moto.repository.OrgReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    public Page<OrgReports> findAll(int offset, int pageSize, String field) {
        return orgReportsRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public List<OrgReports> findByDriverId(String driverId) {
        return orgReportsRepository.findByDriverId(driverId);
    }

    public List<OrgReports> findByDriverName(String name){
        return orgReportsRepository.findByDriverName(name);
    }

    public Double getSumOfCurrentMonth(LocalDateTime startDate, LocalDateTime endDate) {
        return orgReportsRepository.sumAmountForCurrentMonth(startDate, endDate);
    }

    public Double getSumAmountOnDate(LocalDateTime startDate, LocalDateTime endDate) {
        return orgReportsRepository.sumAmountOnDate(startDate, endDate);
    }

    public List<Object[]> findDriverWithHighestAmountForCurrentMonth(LocalDateTime startDate, LocalDateTime endDate) {
        return orgReportsRepository.findDriverWithHighestAmountForCurrentMonth(startDate, endDate);
    }
}
