package com.ot.moto.repository;

import com.ot.moto.entity.FleetHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FleetHistoryRepository extends JpaRepository<FleetHistory, Long> {

    @Query("SELECT fh FROM FleetHistory fh WHERE fh.fleet.id = :fleetId")
    public Page<FleetHistory> findByFleetId(@Param("fleetId") Long fleetId,Pageable pageable);


    @Query("SELECT fh FROM FleetHistory fh WHERE fh.driver.id = :driverId")
    public List<FleetHistory> findByDriverId(@Param("driverId") Long driverId);


    @Query("SELECT fh FROM FleetHistory fh WHERE fh.fleet.id = :fleetId AND fh.driver.id = :driverId")
    public List<FleetHistory> findByFleetIdAndDriverId(@Param("fleetId") Long fleetId, @Param("driverId") Long driverId);


    @Query("SELECT fh FROM FleetHistory fh WHERE fh.fleet.id = :fleetId AND fh.fleetAssignDateTime BETWEEN :startDate AND :endDate")
    public List<FleetHistory> findByFleetIdAndDateRange(
            @Param("fleetId") Long fleetId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    public Page<FleetHistory> findAll(Pageable pageable);

    public List<FleetHistory> findByFleetId(Long fleetId);

    @Modifying
    @Query("UPDATE FleetHistory fh SET fh.driver = null WHERE fh.driver.id = :driverId")
    void nullifyDriverReference(@Param("driverId") Long driverId);

    @Modifying
    @Query("DELETE FROM FleetHistory fh WHERE fh.driver.id = :driverId")
    void deleteFleetHistoryByDriverId(@Param("driverId") Long driverId);


}