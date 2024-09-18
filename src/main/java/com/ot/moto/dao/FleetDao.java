package com.ot.moto.dao;


import com.ot.moto.entity.Fleet;
import com.ot.moto.repository.FleetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
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
        long self = fleetRepository.countByOwnType(Fleet.OWN_TYPE.SELF_OWNED);
        return Objects.isNull(self) ? 0 : self;
    }

    public long getMotoPointFleetCount(Fleet.OWN_TYPE motoPoint) {
        long moto = fleetRepository.countByOwnType(Fleet.OWN_TYPE.MOTO_POINT);
        return Objects.isNull(moto) ? 0 : moto;
    }

    public long countByVehicleType(Fleet.VEHICLE_TYPE vehicleType) {
        long countV = fleetRepository.countByVehicleType(vehicleType);
        return Objects.isNull(countV) ? 0 : countV;
    }

    public List<Fleet> findFleetsByVehicleNumber(String substring) {
        return fleetRepository.findByVehicleNumberContaining(substring);
    }

    public long countAssignedTwoWheeler() {
        long countA = fleetRepository.countAssignedTwoWheeler();
        return Objects.isNull(countA) ? 0 : countA;
    }

    public long countAssignedFourWheeler() {
        long countB = fleetRepository.countAssignedFourWheeler();
        return Objects.isNull(countB) ? 0 : countB;
    }

    public List<Fleet> findAllAssignedFleets(int offset, int pageSize, String field){
        return fleetRepository.findAllAssignedFleets(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public List<Fleet> findAllUnAssignedFleets(int offset, int pageSize, String field){
        return fleetRepository.findAllUnassignedFleets(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }
}