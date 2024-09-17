package com.ot.moto.dao;

import com.ot.moto.entity.FleetHistory;
import com.ot.moto.repository.FleetHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FleetHistoryDao {

    @Autowired
    private FleetHistoryRepository fleetHistoryRepository;


    public FleetHistory findById(Long id) {
        Optional<FleetHistory> fleetHistory = fleetHistoryRepository.findById(id);
        return fleetHistory.orElse(null);
    }

    public List<FleetHistory> findByFleetId(Long fleetId) {
        return fleetHistoryRepository.findByFleetId(fleetId);
    }

    public List<FleetHistory> findByDriverId(Long driverId) {
        return fleetHistoryRepository.findByDriverId(driverId);
    }

    public List<FleetHistory> findByFleetIdAndDriverId(Long fleetId, Long driverId) {
        return fleetHistoryRepository.findByFleetIdAndDriverId(fleetId, driverId);
    }

    public Page<FleetHistory> findAll(int offset, int pageSize, String field) {
        return fleetHistoryRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }
}
