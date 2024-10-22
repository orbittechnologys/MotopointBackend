package com.ot.moto.dao;

import com.ot.moto.dto.TamReportDTO;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Tam;
import com.ot.moto.repository.TamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Repository
public class TamDao {

    @Autowired
    private TamRepository tamRepository;

    public Page<Tam> findAll(int offset, int pageSize, String field) {
        return tamRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public List<Tam> findByDriverNameContaining(String name) {
        return tamRepository.findByDriverNameContaining(name);
    }

    public Double getSumPayInAmountForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Double sumRange = tamRepository.sumPayInAmountOnDate(startDate, endDate);
        return Objects.isNull(sumRange) ? 0 : sumRange ;
    }

    public Double getSumPayInAmountForCurrentMonth(LocalDateTime startDate, LocalDateTime endDate) {
        Double sumMonth = tamRepository.sumPayInAmountForCurrentMonth(startDate, endDate);
        return Objects.isNull(sumMonth) ? 0 : sumMonth ;
    }

    public  Double getSumByDriverAndDate(Driver driver, LocalDate date){
        LocalDateTime startOfDay = date.atStartOfDay(); // 00:00:00 of the day
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // 23:59:59 of the day

        return tamRepository.getSumOfPayInAmountByDriverAndDate(driver.getId(), startOfDay, endOfDay).orElse(0.0);
    }

    public TamReportDTO getTamReportsByDriver(Long driverId, LocalDate startDate, LocalDate endDate){
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00 of the day
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return tamRepository.getTamReportsForDriver(startDateTime,endDateTime,driverId);
    }
}
