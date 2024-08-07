package com.ot.moto.repository;

import com.ot.moto.entity.Fleet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FleetRepository extends JpaRepository<Fleet,Long> {

    public Fleet findByVehicleNumber(String vehicleNumber);
}