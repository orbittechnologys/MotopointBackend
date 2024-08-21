package com.ot.moto.dao;

import com.ot.moto.entity.Master;
import com.ot.moto.repository.MasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public class MasterDao {

    @Autowired
    private MasterRepository masterRepository;

    public Master createMaster(Master master) {
        return masterRepository.save(master);
    }

    public Master getMasterBySlab(String slab){
        Optional<Master> masterOptional = masterRepository.findBySlab(slab);
        return  masterOptional.orElse(null);
    }

    public Master getMasterById(Long id){
        Optional<Master> masterOptional = masterRepository.findById(id);
        return  masterOptional.orElse(null);
    }
}
