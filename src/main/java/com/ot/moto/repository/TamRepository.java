package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Tam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TamRepository extends JpaRepository<Tam, Long> {

    public Tam findBykeySessionId(String session);

    public Page<Tam> findAll(Pageable pageable);

    public List<Tam> findByJahezRiderId(Long jahezRiderId);

    public List<Tam> findByDriverNameContaining(String name);

    @Query("SELECT SUM(t.payInAmount) FROM Tam t WHERE t.dateTime >= :startDate AND t.dateTime <= :endDate")
    public Double sumPayInAmountOnDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.payInAmount) FROM Tam t WHERE t.dateTime >= :startDate AND t.dateTime <= :endDate")
    public Double sumPayInAmountForCurrentMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Custom query method to find the sum of PayToJahez by driver
    @Query("SELECT SUM(t.payInAmount) FROM Tam t WHERE t.driver = :driver")
    public Double findSumPayToJahezByDriver(@Param("driver") Driver driver);

    // Custom query method to find the sum of PaidByTam by driver
    @Query("SELECT SUM(t.payInAmount) FROM Tam t WHERE t.driver = :driver")
    public Double findSumPaidByTamByDriver(@Param("driver") Driver driver);

    @Query("SELECT SUM(t.payInAmount) FROM Tam t WHERE t.dateTime >= :startOfDay AND t.dateTime <= :endOfDay")
    public  Double getTotalPayInAmountForToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

}
