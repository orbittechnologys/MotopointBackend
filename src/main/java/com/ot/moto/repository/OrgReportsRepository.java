package com.ot.moto.repository;

import com.ot.moto.entity.OrgReports;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrgReportsRepository extends JpaRepository<OrgReports, Long> {

    public OrgReports findByDidAndDispatchTime(Long did, LocalDateTime dispatchTime);

    public List<OrgReports> findByDriverId(Long driverId);

    public List<OrgReports> findByDriverNameContaining(String name);

    @Query("SELECT SUM(o.amount) FROM OrgReports o WHERE o.dispatchTime >= :startDate AND o.dispatchTime <= :endDate")
    public Double sumAmountForCurrentMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.amount) FROM OrgReports o WHERE o.dispatchTime BETWEEN :startDate AND :endDate")
    public Double sumAmountOnDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.driverId, o.driverName, SUM(o.amount) as totalAmount " +
            "FROM OrgReports o " +
            "WHERE o.dispatchTime BETWEEN :startDate AND :endDate " +
            "GROUP BY o.driverId, o.driverName " +
            "ORDER BY totalAmount DESC")
    public List<Object[]> findDriverWithHighestAmountForCurrentMonth(@Param("startDate") LocalDateTime startDate,
                                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.amount) FROM OrgReports o WHERE o.dispatchDate >= :startOfDay AND o.dispatchDate <= :endOfDay")
    public Double getTotalAmountForToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    public Page<OrgReports> findByDispatchTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    public Page<OrgReports> findByDispatchTimeBetweenAndDriverId(LocalDateTime startDate, LocalDateTime endDate, Long driverId, Pageable pageable);

    // Method to find all reports by dispatch date range
    public List<OrgReports> findByDispatchTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Method to find all reports by dispatch date range and driver ID
    public List<OrgReports> findByDispatchTimeBetweenAndDriverId(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long driverId
    );
}
