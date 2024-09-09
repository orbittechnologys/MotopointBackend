package com.ot.moto.dao;

import com.ot.moto.entity.VisaType;
import com.ot.moto.repository.VisaTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class VisaDao {

    @Autowired
    private VisaTypeRepository visaTypeRepository;

    public VisaType save(VisaType visaType) {
        return visaTypeRepository.save(visaType);
    }

    public VisaType findById(long id) {
        Optional<VisaType> visaTypeOptional = visaTypeRepository.findById(id);
        return visaTypeOptional.orElse(null);
    }

}
