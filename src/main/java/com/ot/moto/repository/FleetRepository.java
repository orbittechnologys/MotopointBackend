package com.ot.moto.repository;

import com.ot.moto.entity.Fleet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FleetRepository extends JpaRepository<Fleet,Long> {

    public Fleet findByVehicleNumber(String vehicleNumber);

    @Query("SELECT COUNT(f) FROM Fleet f WHERE f.vehicleType = :vehicleType")
    public long countByVehicleType(@Param("vehicleType") Fleet.VEHICLE_TYPE vehicleType);
}