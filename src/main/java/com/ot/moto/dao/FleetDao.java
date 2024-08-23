package com.ot.moto.dao;


import com.ot.moto.entity.Fleet;
import com.ot.moto.repository.FleetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FleetDao {

    @Autowired
    private FleetRepository fleetRepository;

    public Fleet createFleet(Fleet fleet) {
        return fleetRepository.save(fleet);
    }

    public Fleet getVehicleNumber(String vehicleNumber) {
        return fleetRepository.findByVehicleNumber(vehicleNumber);
    }

    public Fleet getFleetById(Long id) {
        Optional<Fleet> adminOptional = fleetRepository.findById(id);
        return adminOptional.orElse(null);
    }

    public List<Fleet> getAllFleet() {
        return fleetRepository.findAll();
    }

    public Page<Fleet> findAll(int offset, int pageSize, String field) {
        return fleetRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public long getSelfOwnedFleetCount(Fleet.OWN_TYPE selfOwned) {
        return fleetRepository.countByOwnType(Fleet.OWN_TYPE.SELF_OWNED);
    }

    public long getMotoPointFleetCount(Fleet.OWN_TYPE motoPoint) {
        return fleetRepository.countByOwnType(Fleet.OWN_TYPE.MOTO_POINT);
    }

    public long countByVehicleType(Fleet.VEHICLE_TYPE vehicleType){
        return fleetRepository.countByVehicleType(vehicleType);
    }
}
