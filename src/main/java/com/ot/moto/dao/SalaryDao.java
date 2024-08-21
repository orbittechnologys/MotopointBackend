package com.ot.moto.dao;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Salary;
import com.ot.moto.repository.SalaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SalaryDao {

    @Autowired
    private SalaryRepository salaryRepository;

    public Salary saveSalary(Salary salary){
        return salaryRepository.save(salary);
    }

    public Salary getSalaryByMonthAndYearAndDriver(Long month, Long year, Driver driver){
        Optional<Salary> salaryOptional = salaryRepository.findByMonthAndYearAndDriver(month,year,driver);
        return salaryOptional.orElse(null);
    }
}
