package com.ot.moto.dao;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Salary;
import com.ot.moto.repository.SalaryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class SalaryDao {

    @Autowired
    private SalaryRepository salaryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Salary saveSalary(Salary salary) {
        return salaryRepository.save(salary);
    }

    public Salary getSalaryByMonthAndYearAndDriver(Long month, Long year, Driver driver) {
        Optional<Salary> salaryOptional = salaryRepository.findByMonthAndYearAndDriver(month, year, driver);
        return salaryOptional.orElse(null);
    }

    public Salary getSalaryByDriverAndDate(Driver driver, LocalDate date) {
        Optional<Salary> salaryOptional = salaryRepository.findByDriverAndSalaryCreditDate(driver, date);
        return salaryOptional.orElse(null);
    }

    public Salary getById(Long id) {
        Optional<Salary> driver = salaryRepository.findById(id);
        return driver.orElse(null);
    }

    public Page<Salary> findAll(int offset, int pageSize, String field) {
        return salaryRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public List<Salary> findHighestBonus() {
        return salaryRepository.findHighestBonus();
    }

    public Double getSumOfSettledSalaries() {
        Double sett = salaryRepository.sumOfSettledSalaries();
        return Objects.isNull(sett) ? 0 : sett;
    }

    public Double getSumOfNotSettledSalaries() {
        Double sal = salaryRepository.sumOfNotSettledSalaries();
        return Objects.isNull(sal) ? 0 : sal;
    }

    public List<Salary> findByDriverUsernameContaining(String username) {
        return salaryRepository.findByDriverUsernameContaining(username);
    }

    public void saveAll(List<Salary> salaries) {
        salaryRepository.saveAll(salaries);
    }

    public Double getTotalPayableAmountSettledBetweenDates(LocalDate startDate, LocalDate endDate) {
        return salaryRepository.getTotalPayableAmountSettledBetweenSalaryCreditDate(startDate, endDate);
    }

    public Double getTotalPayableAmountNotSettledBetweenDates(LocalDate startDate, LocalDate endDate) {
        return salaryRepository.getTotalPayableAmountNotSettledBetweenSalaryCreditDate(startDate, endDate);
    }

    public void flush() {
        entityManager.flush();
    }

    public void clear() {
        entityManager.clear();
    }
}
