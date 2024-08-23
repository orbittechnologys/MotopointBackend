package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    public Optional<Salary> findByMonthAndYearAndDriver(Long month, Long year, Driver driver);

    @Query(value = "SELECT * FROM Salary WHERE bonus = (SELECT MAX(bonus) FROM Salary) LIMIT 1", nativeQuery = true)
    public Salary findHighestBonus();


    @Query("SELECT s FROM Salary s WHERE s.driver IN (SELECT d FROM Driver d WHERE d.fleet.vehicleNumber = :vehicleNumber)")
    public List<Salary> findSalariesByVehicleNumber(@Param("vehicleNumber") String vehicleNumber);


    @Query("SELECT SUM(s.totalEarnings) FROM Salary s WHERE s.status = 'SETTLED'")
    public List<Double> sumOfSettledSalaries();


    @Query("SELECT SUM(s.totalEarnings) FROM Salary s WHERE s.status = 'NOT_SETTLED'")
    public Double sumOfNotSettledSalaries();
}
