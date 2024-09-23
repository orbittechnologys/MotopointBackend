package com.ot.moto.dao;

import com.ot.moto.entity.OtherDeduction;
import com.ot.moto.repository.OtherDeductionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OtherDeductionDao {

    @Autowired
    private OtherDeductionRepository otherDeductionRepository;

    public OtherDeduction findById(Long id) {
        Optional<OtherDeduction> otherDeduction = otherDeductionRepository.findById(id);
        return otherDeduction.orElse(null);
    }

}