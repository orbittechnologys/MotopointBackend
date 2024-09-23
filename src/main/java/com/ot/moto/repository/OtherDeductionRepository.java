package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.OtherDeduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtherDeductionRepository extends JpaRepository<OtherDeduction, Long> {

    public List<OtherDeduction> findByDriver(Driver driver);

    public List<OtherDeduction> findByDriverId(Long driverId);
}
