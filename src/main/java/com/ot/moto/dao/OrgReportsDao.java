package com.ot.moto.dao;


import com.ot.moto.entity.OrgReports;
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

    public List<OrgReports> findByDriverNameContaining(String name) {
        return orgReportsRepository.findByDriverNameContaining(name);
    }

    public Double getSumOfCurrentMonth(LocalDateTime startDate, LocalDateTime endDate) {
        Double sum = orgReportsRepository.sumAmountForCurrentMonth(startDate, endDate);
        return (sum != null) ? sum : 0.0;
    }

    public Double getSumAmountOnDate(LocalDateTime startDate, LocalDateTime endDate) {
        Double sum = orgReportsRepository.sumAmountOnDate(startDate, endDate);
        return (sum != null) ? sum : 0.0;
    }

    public List<Object[]> findDriverWithHighestAmountForCurrentMonth(LocalDateTime startDate, LocalDateTime endDate) {
        return orgReportsRepository.findDriverWithHighestAmountForCurrentMonth(startDate, endDate);
    }
}