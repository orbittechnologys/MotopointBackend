package com.ot.moto.dao;

import com.ot.moto.entity.Tam;
import com.ot.moto.repository.TamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TamDao {

    @Autowired
    private TamRepository tamRepository;

    public Page<Tam> findAll(int offset, int pageSize, String field) {
        return tamRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public List<Tam> findByDriverNameContaining(String name){
        return tamRepository.findByDriverNameContaining(name);
    }
    public Double getSumPayInAmountForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return tamRepository.sumPayInAmountOnDate(startDate, endDate);
    }

    public Double getSumPayInAmountForCurrentMonth(LocalDateTime startDate, LocalDateTime endDate) {
        return tamRepository.sumPayInAmountForCurrentMonth(startDate, endDate);
    }
}
