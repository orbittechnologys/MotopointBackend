package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    public Driver findByPhone(String phone);

    public List<Driver> findByUsernameContaining(String username);

    @Query("SELECT d FROM Driver d WHERE LOWER(d.username) = LOWER(:username)")
    public Optional<Driver> findByNameIgnoreCase(@Param("username") String username);

    @Query("SELECT d FROM Driver d JOIN User u ON d.id = u.id WHERE LOWER(u.username) = LOWER(:username)")
    public List<Driver> findByNameIgnoreCaseList(@Param("username") String username);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.visa.visaName = 'COMPANY'")
    public Long countDriversWithCompanyVisa();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.visa.visaName = 'CR'")
    public Long countDriversWithCrVisa();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.visa.visaName = 'OTHER'")
    public Long countDriversWithOtherVisa();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.visa.visaName = 'FLEXI'")
    public Long countDriversWithFlexiVisa();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.vehicleType = '2WHEELER'")
    public Long countTwoWheelerRiders();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.vehicleType = '4WHEELER'")
    public Long countFourWheelerDrivers();

    @Query("SELECT COUNT(d) FROM Driver d")
    public Long countTotalDrivers();

    @Query("SELECT d FROM Driver d ORDER BY d.totalOrders DESC LIMIT 1")
    public Optional<Driver> findTopDriverByTotalOrders();

    @Query("SELECT d FROM Driver d ORDER BY d.currentOrders DESC LIMIT 1")
    public Optional<Driver> findTopDriverByCurrentOrders();

    @Query("SELECT SUM(d.payToJahez) FROM Driver d")
    public Double sumPayToJahezForAllDrivers();

    @Query("SELECT SUM(d.profit) FROM Driver d")
    public Double sumProfitForAllDrivers();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.vehicleType = 'owned'")
    public Long countByVehicleTypeOwned();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.vehicleType != 'owned'")
    public Long countByVehicleTypeNotOwned();

    @Query("SELECT d FROM Driver d WHERE d.vehicleType IN ('S-Rented','Rented')")
    public Page<Driver> findDriversByRentedSRentedVehicleType(Pageable pageable);

    public Driver findByJahezId(Long jahezId);


    /*@Query("SELECT SUM(o.codAmount) FROM Orders o")
    public Double sumCodAmount();

    @Query("SELECT COUNT(o) FROM Orders o")
    public Long sumTotalOrders(); // Use COUNT if totalOrders is derived from the count of orders.

    @Query("SELECT SUM(b.bonus) FROM Bonus b")
    public Double sumBonus();*/

   /* @Query("SELECT SUM(p.penalties) FROM Penalty p")
    public Double sumPenalties();*/

    /*@Query("SELECT COUNT(d) FROM Driver d WHERE d.vehicleType = 'bike'")
    public Long countByVehicleTypeBike();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.vehicleType = 'visa'")
    public Long countByVehicleTypeVisa();

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.vehicleType != 'bike' AND d.vehicleType != 'visa'")
    public Long countByVehicleTypeOther();

    @Query("SELECT SUM(d.driverAmountPending) FROM Driver d")
    public Double sumDriverAmountPending();*/
}
