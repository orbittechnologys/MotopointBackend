package com.ot.moto.repository;

import com.ot.moto.entity.Fleet;
import org.springframework.data.jpa.repository.JpaRepository;
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
}