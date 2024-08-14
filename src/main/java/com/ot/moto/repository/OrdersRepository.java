package com.ot.moto.repository;

import com.ot.moto.entity.Orders;
import jakarta.persistence.criteria.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders,Long> {

    public Optional<Orders> findByDateAndDriverName(LocalDate date, String driverName);

    public Page<Orders> findAll(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o.driver) FROM Orders o WHERE o.date = ?1")
    public long countDriversWithOrdersOnDate(LocalDate date);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.date = :date")
    public long countOrdersOnDate(LocalDate date);
    @Query("SELECT COUNT(o) FROM Orders o WHERE o.date BETWEEN :startDate AND :endDate")
    long countOrdersBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
