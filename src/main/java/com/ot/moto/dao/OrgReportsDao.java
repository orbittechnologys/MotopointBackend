package com.ot.moto.dao;


import com.ot.moto.dto.DriverReportDTO;
import com.ot.moto.entity.OrgReports;
import com.ot.moto.repository.OrgReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    public List<OrgReports> findByDriverId(Long driverId) {
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

 /*   public Page<OrgReports> findAllBetweenDates(LocalDateTime startDate, LocalDateTime endDate, int page, int size, String field) {
        return orgReportsRepository.findAllBetweenDispatchDate(startDate,endDate,PageRequest.of(page, size).withSort(Sort.by(field).descending()));
    }

    public Page<OrgReports> findReportsForDriverBetweenDates(Long driverId, LocalDateTime startDate, LocalDateTime endDate, int page, int size, String field) {
        return orgReportsRepository.findReportsForDriverBetweenDispatchDate(driverId,startDate,endDate,PageRequest.of(page, size).withSort(Sort.by(field).descending()));
    }*/

    public List<DriverReportDTO> getDriverReports(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return orgReportsRepository.getDriverReports(startDateTime, endDateTime);
    }

    public DriverReportDTO getDriverReportsByDriver(String driverId, LocalDate startDate, LocalDate endDate){
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return orgReportsRepository.getDriverReportsForDriver(startDateTime,endDateTime,driverId);
    }
}