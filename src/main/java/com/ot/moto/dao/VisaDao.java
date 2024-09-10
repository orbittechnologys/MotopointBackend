package com.ot.moto.dao;

import com.ot.moto.entity.Visa;
import com.ot.moto.repository.VisaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VisaDao {

    @Autowired
    private VisaRepository visaRepository;

    public Visa save(Visa visa) {
        return visaRepository.save(visa);
    }

    public Visa findById(long id) {
        Optional<Visa> optional = visaRepository.findById(id);
        return optional.orElse(null);
    }

    public List<Visa> findAll() {
        return visaRepository.findAll();
    }

    public Visa findByVisaName(String visaName) {
        Optional<Visa> optional = visaRepository.findByVisaNameIgnoreCase(visaName);
        return optional.orElse(null);
    }
}