package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    public Optional<Summary> findByDriver(Driver driver);

    @Query("SELECT COALESCE(SUM(s.payToJahez), 0) FROM Summary s")
    public Double findTotalPayToJahez();

    @Query("SELECT COALESCE(SUM(s.salary), 0) FROM Summary s")
    public Double findTotalSalaryPaid();

    @Query("SELECT COALESCE(SUM(s.profit), 0) FROM Summary s")
    public Double findTotalProfit();


}
