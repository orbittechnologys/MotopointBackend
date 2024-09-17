package com.ot.moto.repository;

import com.ot.moto.entity.Fleet;
import com.ot.moto.entity.FleetHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FleetHistoryRepository extends JpaRepository<FleetHistory, Long> {

    public List<FleetHistory> findByFleetId(Long fleetId);

    public List<FleetHistory> findByDriverId(Long driverId);

    public List<FleetHistory> findByFleetIdAndDriverId(Long fleetId, Long driverId);

    public Page<FleetHistory> findAll(Pageable pageable);
}