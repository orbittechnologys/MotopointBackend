package com.ot.moto.repository;

import com.ot.moto.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    public boolean existsByDriverIdAndDate(Long driverId, LocalDate date);
}
