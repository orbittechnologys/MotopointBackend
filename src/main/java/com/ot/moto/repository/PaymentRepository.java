package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    public boolean existsByDriverIdAndDate(Long driverId, LocalDate date);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.date = :date")
    public Double sumAmountOnDate(@Param("date") LocalDate date);

    @Query("SELECT p.type, SUM(p.amount) FROM Payment p GROUP BY p.type")
    public List<Object[]> getTotalAmountByPaymentType();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.date >= :startDate AND p.date <= :endDate")
    public Double sumAmountForCurrentMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Payment p JOIN p.driver d WHERE LOWER(TRIM(d.username)) LIKE LOWER(CONCAT('%', :username, '%'))")
    public List<Payment> findPaymentsByDriverNameContaining(@Param("username") String username);

    @Query("SELECT SUM(t.payInAmount) FROM Tam t WHERE t.dateTime >= :startOfDay AND t.dateTime < :endOfDay")
    public Double sumPayInAmountForYesterday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.date = :today")
    public Double getTotalAmountForToday(@Param("today") LocalDate today);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.driver.id = :driverId")
    public Double findTotalAmountByDriver(@Param("driverId") Long driverId);

    @Query("SELECT SUM(amount) FROM Payment")
    public Double findTotalAmount();

    public List<Payment> findAllByDriverIdAndDateBetween(Long driverId, LocalDate startDate, LocalDate endDate);

    public List<Payment> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    public List<Payment> findByDriverId(Long driverId);

    public Page<Payment> findAllByDriverIdAndDateBetween(Long driverId, LocalDate startDate, LocalDate endDate, PageRequest pageRequest);

    public Page<Payment> findAllByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.driver.id = :driverId AND p.date BETWEEN :startDate AND :endDate")
    public List<Payment> findByDriverIdAndDateBetween(
            @Param("driverId") Long driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    public Optional<Payment> findByDriverAndDate(Driver driver, LocalDate date);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.driver.id = :driverId " +
            "AND p.date = :date")
    public Optional<Double> getSumOfAmountByDriverAndDate(
            @Param("driverId") Long driverId,
            @Param("date") LocalDate date);
}