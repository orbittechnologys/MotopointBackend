package com.ot.moto.repository;

import com.ot.moto.dto.DriverReportDTO;
import com.ot.moto.dto.PaymentReportDTO;
import com.ot.moto.dto.TamReportDTO;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Tam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
    public Double getTotalPayInAmountForToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    public List<Tam> findAllByDriverIdAndDateTimeBetween(Long driverId, LocalDateTime startDate, LocalDateTime endDate);

    public List<Tam> findAllByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    public Page<Tam> findAllByDriverIdAndDateTimeBetween(Long driverId, LocalDateTime startDateTime, LocalDateTime endDateTime,Pageable pageable);

    public Page<Tam> findAllByDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime,Pageable pageable);

    public List<Tam> findByDriverId(Long driverId);

    @Query("SELECT t FROM Tam t WHERE t.driver.id = :driverId AND CAST(t.dateTime AS LocalDate) BETWEEN :startDate AND :endDate")
    public List<Tam> findAllByDriverIdAndDateBetween(
            @Param("driverId") Long driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(t.payInAmount) FROM Tam t WHERE t.driver.id = :driverId " +
            "AND t.confTrxnDateTime BETWEEN :startOfDay AND :endOfDay")
    public Optional<Double> getSumOfPayInAmountByDriverAndDate(
            @Param("driverId") Long driverId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);


    @Query("SELECT new com.ot.moto.dto.TamReportDTO( "
            + "u.username, "
            + "SUM(t.payInAmount), "
            + "d.id "  // Accessing driverId through the Driver entity
            + ") "
            + "FROM Tam t "
            + "JOIN t.driver d "  // Use p.driver instead of p.driverId
            + "JOIN User u ON d.id = u.id "
            + "WHERE t.confTrxnDateTime BETWEEN :startDateTime AND :endDateTime "
            + "GROUP BY d.id, u.username")
    public List<TamReportDTO> getTamReports(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);


    @Query("SELECT new com.ot.moto.dto.TamReportDTO( "
            + "u.username, "
            + "SUM(t.payInAmount), "
            + "d.id "  // Accessing driverId through the Driver entity
            + ") "
            + "FROM Tam t "
            + "JOIN t.driver d "  // Use p.driver instead of p.driverId
            + "JOIN User u ON d.id = u.id "
            + "WHERE t.confTrxnDateTime BETWEEN :startDateTime AND :endDateTime AND d.id = :driverId "
            + "GROUP BY d.id, u.username")
    public TamReportDTO getTamReportsForDriver(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("driverId") Long driverId);


}
