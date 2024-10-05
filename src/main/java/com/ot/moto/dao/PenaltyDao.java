package com.ot.moto.dao;

import com.ot.moto.entity.OrgReports;
import com.ot.moto.entity.Penalty;
import com.ot.moto.repository.PenaltyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PenaltyDao {

    @Autowired
    private PenaltyRepository penaltyRepository;

    public Penalty save(Penalty penalty) {
        return penaltyRepository.save(penalty);
    }

    public List<Penalty> saveAll(List<Penalty> penalties) {
        return penaltyRepository.saveAll(penalties);
    }

    public void delete(Penalty penalty) {
        penaltyRepository.delete(penalty);
    }

    public Penalty findById(long id) {
        Optional<Penalty> penaltyOptional = penaltyRepository.findById(id);
        return penaltyOptional.orElse(null);
    }

    public List<Penalty> findAll() {
        return penaltyRepository.findAll();
    }

    public List<Penalty> getPenaltiesByFleetIdAndDriverId(long fleetId, long driverId) {
        return penaltyRepository.findByFleetIdAndDriverId(fleetId, driverId);
    }

    public void deleteByFleetIdAndDriverId(Long fleetId, Long driverId) {
        penaltyRepository.deleteByFleetIdAndDriverId(fleetId, driverId);
    }

    public void deleteByFleetId(Long fleetId) {
        penaltyRepository.deleteByFleetId(fleetId);
    }

    public Page<Penalty> findByFleetId(Long fleetId, int offset, int pageSize, String field) {
        return penaltyRepository.findByFleetId(fleetId, PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public Page<Penalty> findByDriverId(Long driverId, int offset, int pageSize, String field) {
        return penaltyRepository.findByDriverId(driverId, PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public List<Penalty> getPenaltiesByFleetId(Long fleetId) {
        return penaltyRepository.getByFleetId(fleetId);
    }

    public void deleteAll(List<Penalty> penalties) {
         penaltyRepository.deleteAll();
    }

    public List<Penalty> findByDriverId(Long driverId){
        return penaltyRepository.findByDriverId(driverId);
    }
}

