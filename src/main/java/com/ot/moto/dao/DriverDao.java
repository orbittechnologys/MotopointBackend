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
        return driverRepository.countFlexiVisa();
    }

    public long countOtherVisaTypes() {
        return driverRepository.countOtherVisaTypes();
    }

    public long countTwoWheelerRiders() {
        return driverRepository.countTwoWheelerRiders();
    }

    public long countFourWheelerDrivers() {
        return driverRepository.countFourWheelerDrivers();
    }

    public long countTotalDrivers() {
        return driverRepository.countTotalDrivers();
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
}

//public Double sumPaidByTamForAllDrivers() {
//    Double sum = driverRepository.sumPaidByTamForAllDrivers();
//    return Objects.isNull(sum) ? 0 : sum;
//}
