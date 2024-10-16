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

    public Page<Orders> findAllByDriverIdAndDateBetween(Long driverId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o.driver) FROM Orders o WHERE o.date = ?1")
    public long countDriversWithOrdersOnDate(LocalDate date);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.date = :date")
    public long countOrdersOnDate(LocalDate date);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.date BETWEEN :startDate AND :endDate")
    public long countOrdersBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

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

    @Query("SELECT o.driver.id, o.driver.username, o.driver.profilePic, SUM(o.totalOrders) " +
            "FROM Orders o " +
            "GROUP BY o.driver.id, o.driver.username, o.driver.profilePic")
    public List<Object[]> findTotalOrdersForAllDrivers();


    @Query("SELECT o.driver.id, o.driver.username, SUM(o.totalOrders) " +
            "FROM Orders o " +
            "GROUP BY o.driver.id, o.driver.username " +
            "ORDER BY SUM(o.totalOrders) DESC")
    public List<Object[]> findDriverWithHighestTotalOrders();

    @Query("SELECT COALESCE(SUM(o.totalOrders), 0) FROM Orders o WHERE o.driver.id = :driverId AND o.date >= :startDate AND o.date <= :endDate")
    public Long findTotalOrdersForCurrentMonthByDriver(Long driverId, LocalDate startDate, LocalDate endDate);

    public Optional<Orders> findByDriverNameAndDate(String driverName, LocalDate date);

    public Page<Orders> findAllByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    public List<Orders> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT o FROM Orders o WHERE o.driver.id = :driverId AND o.date BETWEEN :startDate AND :endDate")
    public List<Orders> findByDriverIdAndOrderDateBetween(
            @Param("driverId") Long driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    public List<Orders> findByDriverId(Long driverId);

    public List<Orders> findAllByDriverIdAndDateBetween(Long DriverId,LocalDate startDate,LocalDate endDate);
}
