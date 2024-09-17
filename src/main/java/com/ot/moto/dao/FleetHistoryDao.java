package com.ot.moto.dao;

import com.ot.moto.entity.FleetHistory;
import com.ot.moto.repository.FleetHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class FleetHistoryDao {

    @Autowired
    private FleetHistoryRepository fleetHistoryRepository;

    public Page<FleetHistory> findAll(int offset, int pageSize, String field) {
        return fleetHistoryRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }
}
