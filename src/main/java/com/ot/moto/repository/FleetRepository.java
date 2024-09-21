package com.ot.moto.repository;

import com.ot.moto.entity.Fleet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FleetRepository extends JpaRepository<Fleet, Long> {

    public Fleet findByVehicleNumber(String vehicleNumber);

    @Query("SELECT COUNT(f) FROM Fleet f WHERE f.ownType = :ownType")
    public long countByOwnType(@Param("ownType") Fleet.OWN_TYPE ownType);

    @Query("SELECT COUNT(f) FROM Fleet f WHERE f.vehicleType = :vehicleType")
    public long countByVehicleType(@Param("vehicleType") Fleet.VEHICLE_TYPE vehicleType);

    public List<Fleet> findByVehicleNumberContaining(String substring);

    @Query("SELECT COUNT(f) FROM Fleet f WHERE f.vehicleType = com.ot.moto.entity.Fleet.VEHICLE_TYPE.TWO_WHEELER AND f.driver IS NOT NULL")
    public long countAssignedTwoWheeler();

    @Query("SELECT COUNT(f) FROM Fleet f WHERE f.vehicleType = com.ot.moto.entity.Fleet.VEHICLE_TYPE.FOUR_WHEELER AND f.driver IS NOT NULL")
    public long countAssignedFourWheeler();

    @Query("SELECT f FROM Fleet f WHERE f.driver IS NOT NULL")
    public List<Fleet> findAllAssignedFleets(Pageable pageable);

    @Query("SELECT f FROM Fleet f WHERE f.driver IS NULL")
    public List<Fleet> findAllUnassignedFleets(Pageable pageable);

    @Query("SELECT f FROM Fleet f WHERE f.driver.id = :driverId")
    List<Fleet> findFleetByDriverId(@Param("driverId") Long driverId);

    @Modifying
    @Query("UPDATE Fleet f SET f.driver = null WHERE f.driver.id = :driverId")
    void nullifyFleetDriver(@Param("driverId") Long driverId);


}