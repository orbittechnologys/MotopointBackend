package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    public Optional<Orders> findByDateAndDriverName(LocalDate date, String driverName);

    public Page<Orders> findAll(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o.driver) FROM Orders o WHERE o.date = ?1")
    public long countDriversWithOrdersOnDate(LocalDate date);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.date = :date")
    public long countOrdersOnDate(LocalDate date);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.date BETWEEN :startDate AND :endDate")
    long countOrdersBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(o.totalOrders) FROM Orders o WHERE o.date = :date")
    public Long sumTotalOrdersOnDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(o.totalOrders) FROM Orders o WHERE o.date BETWEEN :startDate AND :endDate")
    public Long sumTotalOrdersBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(o.codAmount) FROM Orders o WHERE o.date = :date")
    public Double sumAmountOnDate(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT o.driver FROM Orders o WHERE o.date = ?1")
    public List<Driver> findDistinctDriversWithOrdersOnDate(LocalDate date);


    @Query("SELECT o.driver, COUNT(DISTINCT o.date) FROM Orders o WHERE MONTH(o.date) = MONTH(?1) AND YEAR(o.date) = YEAR(?1) GROUP BY o.driver")
    public List<Object[]> findDriverAttendanceForCurrentMonth(LocalDate date);

    public List<Orders> findByDriverNameContaining(String letter);

    public Long countByDriver(Driver driver);
}
