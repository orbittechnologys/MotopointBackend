package com.ot.moto.repository;

import com.ot.moto.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders,Long> {

    public Optional<Orders> findByDateAndDriverName(LocalDate date, String driverName);
}
