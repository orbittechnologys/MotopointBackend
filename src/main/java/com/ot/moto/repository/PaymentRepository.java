package com.ot.moto.repository;

import com.ot.moto.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    public boolean existsByDriverIdAndDate(Long driverId, LocalDate date);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.date = :date")
    public Double sumAmountOnDate(@Param("date") LocalDate date);

    @Query("SELECT p.type, SUM(p.amount) FROM Payment p GROUP BY p.type")
    public List<Object[]> getTotalAmountByPaymentType();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.date >= :startDate AND p.date <= :endDate")
    public Double sumAmountForCurrentMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Payment p JOIN p.driver d WHERE LOWER(d.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    public List<Payment> findPaymentsByDriverNameContaining(@Param("username") String username);
}