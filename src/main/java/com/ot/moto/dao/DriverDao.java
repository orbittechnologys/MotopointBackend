package com.ot.moto.dao;

import com.ot.moto.entity.Admin;
import com.ot.moto.entity.Driver;
import com.ot.moto.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public class DriverDao {

    @Autowired
    private DriverRepository driverRepository;

    public Driver createDriver(Driver driver){
        return driverRepository.save(driver);
    }

    public Driver getById(Long id){
        Optional<Driver> driver = driverRepository.findById(id);
        return driver.orElse(null);
    }

    public Page<Driver> findAll(int offset, int pageSize, String field) {
        return driverRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public Driver findByNameIgnoreCase(String driverName){
        Optional<Driver> driverOptional = driverRepository.findByNameIgnoreCase(driverName);
        return driverOptional.orElse(null);
    }
}
