package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Fleet;
import com.ot.moto.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    @Query("SELECT p FROM Penalty p WHERE p.fleet.id = :fleetId")
    public List<Penalty> findByFleetId(@Param("fleetId") Long fleetId);

    public List<Penalty> findByFleet(Fleet fleet);

    public List<Penalty> findByDriver(Driver driver);

    @Query("SELECT p FROM Penalty p WHERE p.fleet.id = :fleetId AND p.driver.id = :driverId")
    public List<Penalty> findByFleetIdAndDriverId(@Param("fleetId") Long fleetId, @Param("driverId") Long driverId);

    @Modifying
    @Query("DELETE FROM Penalty p WHERE p.fleet.id = :fleetId AND p.driver.id = :driverId")
    public void deleteByFleetIdAndDriverId(@Param("fleetId") Long fleetId, @Param("driverId") Long driverId);
}