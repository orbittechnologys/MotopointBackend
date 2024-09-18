package com.ot.moto.dao;

import com.ot.moto.entity.Driver;
import com.ot.moto.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class DriverDao {

    @Autowired
    private DriverRepository driverRepository;

    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    public Driver getById(Long id) {
        Optional<Driver> driver = driverRepository.findById(id);
        return driver.orElse(null);
    }

    public Page<Driver> findAll(int offset, int pageSize, String field) {
        return driverRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }

    public Driver findByNameIgnoreCase(String driverName) {
        Optional<Driver> driverOptional = driverRepository.findByNameIgnoreCase(driverName);
        return driverOptional.orElse(null);
    }

    public long countFlexiVisa() {
        Long countFl = driverRepository.countDriversWithFlexiVisa();
        return countFl != null ? countFl : 0;
    }

    public long countCompanyVisa() {
        Long countFl = driverRepository.countDriversWithCompanyVisa();
        return countFl != null ? countFl : 0;
    }

    public long countCrVisa() {
        Long countFl = driverRepository.countDriversWithCrVisa();
        return countFl != null ? countFl : 0;
    }

    public long countOtherVisa() {
        Long countFl = driverRepository.countDriversWithOtherVisa();
        return countFl != null ? countFl : 0;
    }

    public long countTwoWheelerRiders() {
        long countT = driverRepository.countTwoWheelerRiders();
        return Objects.isNull(countT) ? 0 : countT;
    }

    public long countFourWheelerDrivers() {
        long countF = driverRepository.countFourWheelerDrivers();
        return Objects.isNull(countF) ? 0 : countF;
    }

    public long countTotalDrivers() {
        long countD = driverRepository.countTotalDrivers();
        return Objects.isNull(countD) ? 0 : countD;
    }

    public Driver findByPhoneNumber(String phoneNumber) {
        return driverRepository.findByPhone(phoneNumber);
    }

    public void deleteDriver(Driver driver) {
        driverRepository.delete(driver);
    }

    public Driver findTopDriverByTotalOrders() {
        Optional<Driver> optionalDriver = driverRepository.findTopDriverByTotalOrders();
        return optionalDriver.orElse(null);
    }

    public Driver findTopDriversByCurrentOrders() {
        Optional<Driver> optionalDriver = driverRepository.findTopDriverByCurrentOrders();
        return optionalDriver.orElse(null);
    }

    public List<Driver> findByUsernameContaining(String name) {
        return driverRepository.findByUsernameContaining(name);
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public Double SumPayToJahezForAllDrivers() {
        Double amount = driverRepository.sumPayToJahezForAllDrivers();
        return Objects.isNull(amount) ? 0 : amount;
    }

    public Double sumProfitForAllDrivers() {
        Double sum = driverRepository.sumProfitForAllDrivers();
        return Objects.isNull(sum) ? 0 : sum;
    }


    public Long countOwnedVehicleDrivers() {
        Long sum = driverRepository.countByVehicleTypeOwned();
        return (sum == null) ? 0 : sum;
    }


    public Long countDriversWithVehicleTypeNotOwned() {
        Long count = driverRepository.countByVehicleTypeNotOwned();
        return (count == null) ? 0L : count;
    }


    public Page<Driver> rentedSRented(int offset,int pageSize,String field) {
        return driverRepository.findDriversByRentedSRentedVehicleType(PageRequest.of(offset,pageSize).withSort(Sort.by(field).descending()));
    }
}