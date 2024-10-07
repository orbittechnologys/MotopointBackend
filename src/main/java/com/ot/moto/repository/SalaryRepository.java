package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Salary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    public Optional<Salary> findByMonthAndYearAndDriver(Long month, Long year, Driver driver);

    public Optional<Salary> findByDriverAndSalaryCreditDate(Driver driver,LocalDate salaryCreditedDate);

    @Query(value = "SELECT s FROM Salary s WHERE s.bonus = (SELECT MAX(bonus) FROM Salary)")
    public List<Salary> findHighestBonus();

    @Query("SELECT s FROM Salary s WHERE s.driver.username LIKE %:driverName%")
    List<Salary> findByDriverUsernameContaining(@Param("driverName") String driverName);

    @Query("SELECT SUM(s.totalEarnings) FROM Salary s WHERE s.status = 'SETTLED'")
    public Double sumOfSettledSalaries();

    @Query("SELECT SUM(s.totalEarnings) FROM Salary s WHERE s.status = 'NOT_SETTLED'")
    public Double sumOfNotSettledSalaries();

    public Optional<Salary> findByDriverId(Long driverId);

    public Optional<Salary> findFirstByDriverIdOrderByYearDescMonthDesc(Long driverId);

    public Optional<Salary> findTopByDriverOrderByYearDescMonthDesc(Driver driver);

    public Page<Salary> findBySalaryCreditDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, String status, Pageable pageable);

    public Page<Salary> findBySalaryCreditDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    public List<Salary> findBySalaryCreditDateBetween(LocalDate startDate, LocalDate endDate);

    public List<Salary> findByDriverIdAndSalaryCreditDateBetween(Long driverId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(s.payableAmount) FROM Salary s WHERE s.status = 'SETTLED' AND s.salaryCreditDate BETWEEN :startDate AND :endDate")
    public Double getTotalPayableAmountSettledBetweenSalaryCreditDate(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(s.payableAmount) FROM Salary s WHERE s.status = 'NOT_SETTLED' AND s.salaryCreditDate BETWEEN :startDate AND :endDate")
    public Double getTotalPayableAmountNotSettledBetweenSalaryCreditDate(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(s.payableAmount) FROM Salary s WHERE s.driver = :driver AND s.status = 'SETTLED' AND s.salaryCreditDate BETWEEN :startDate AND :endDate")
    public Double getTotalPayableAmountSettledForDriverSalaryCreditedDate(@Param("driver") Driver driver, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(s.payableAmount) FROM Salary s WHERE s.driver = :driver AND s.status = 'NOT_SETTLED' AND s.salaryCreditDate BETWEEN :startDate AND :endDate")
    public Double getTotalPayableAmountNotSettledForDriverBetweenSalaryCreditedDate(@Param("driver") Driver driver, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


}