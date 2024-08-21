package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    public Optional<Salary> findByMonthAndYearAndDriver(Long month, Long year, Driver driver);
}
