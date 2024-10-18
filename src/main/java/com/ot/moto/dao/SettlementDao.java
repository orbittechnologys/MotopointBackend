package com.ot.moto.dao;

import com.ot.moto.entity.Settlement;
import com.ot.moto.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class SettlementDao {

    @Autowired
    private SettlementRepository settlementRepository;

    public Settlement save(Settlement settlement) {
        return settlementRepository.save(settlement);
    }

    public Settlement findById(Long id) {
        Optional<Settlement> settlement = settlementRepository.findById(id);
        return settlement.orElse(null);
    }

    public Page<Settlement> findAll(int page, int size, String field) {
        return settlementRepository.findAll(PageRequest.of(page, size).withSort(Sort.by(field)));
    }

    public Page<Settlement> findAllBySettleDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, int page, int size, String field) {
        return settlementRepository.findBySettleDateTimeBetween(startDate, endDate, PageRequest.of(page, size).withSort(Sort.by(field)));
    }

    public Page<Settlement> findAllSettlementByDriverId(Long driverId, int page, int size, String field) {
        return settlementRepository.findAllByDriverId(driverId, PageRequest.of(page, size).withSort(Sort.by(field)));
    }

    public Page<Settlement> findAllSettlementByDriverIdDateTimeBetween(Long driverId, LocalDateTime startDate, LocalDateTime endDate, int page, int size, String field) {
        return settlementRepository.findAllByDriverIdAndSettleDateTimeBetween(driverId, startDate, endDate, PageRequest.of(page, size).withSort(Sort.by(field)));
    }
}
