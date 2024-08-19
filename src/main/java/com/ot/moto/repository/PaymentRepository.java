package com.ot.moto.repository;

import com.ot.moto.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    public boolean existsByDriverIdAndDate(Long driverId, LocalDate date);

    @Query("SELECT p.type, SUM(p.amount) FROM Payment p GROUP BY p.type")
    List<Object[]> getTotalAmountByPaymentType();
}
