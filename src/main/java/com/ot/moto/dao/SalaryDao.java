package com.ot.moto.dao;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Salary;
import com.ot.moto.repository.SalaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    public Salary getById(Long id){
        Optional<Salary> driver = salaryRepository.findById(id);
        return driver.orElse(null);
    }

    public Page<Salary> findAll(int offset, int pageSize, String field) {
        return salaryRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public Salary findHighestBonus() {
        return salaryRepository.findHighestBonus();
    }

    public List<Double> getSumOfSettledSalaries() {
        return salaryRepository.sumOfSettledSalaries();
    }
    public Double getSumOfNotSettledSalaries() {
        return salaryRepository.sumOfNotSettledSalaries();
    }
}
