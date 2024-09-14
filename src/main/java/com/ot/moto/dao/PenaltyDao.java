package com.ot.moto.dao;

import com.ot.moto.entity.Penalty;
import com.ot.moto.repository.PenaltyRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
