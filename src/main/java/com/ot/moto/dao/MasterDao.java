package com.ot.moto.dao;

import com.ot.moto.entity.Master;
import com.ot.moto.repository.MasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MasterDao {

    @Autowired
    private MasterRepository masterRepository;

    public Master createMaster(Master master) {
        return masterRepository.save(master);
    }

    public Master getMasterBySlab(String slab) {
        Optional<Master> masterOptional = masterRepository.findBySlab(slab);
        return masterOptional.orElse(null);
    }

    public Master getMasterById(Long id) {
        Optional<Master> masterOptional = masterRepository.findById(id);
        return masterOptional.orElse(null);
    }

    public Page<Master> findAll(int offset, int pageSize, String field) {
        return masterRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).ascending()));
    }

    public void delete(Master master) {
        masterRepository.delete(master);
    }

    public Master updateMaster(Master existingMaster) {
        return masterRepository.save(existingMaster);
    }
}
