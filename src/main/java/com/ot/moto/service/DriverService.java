package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.UserDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateDriverReq;
import com.ot.moto.dto.request.UpdateDriverReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.User;
import com.ot.moto.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DriverDao driverDao;

    public ResponseEntity<ResponseStructure<Object>> createDriver(CreateDriverReq request) {
        try {
            if (userDao.checkUserExists(request.getEmail(), request.getPhone())) {
                logger.warn("Email/ Phone already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Email/ Phone already exists");
            }
            Driver driver = buildDriverFromRequest(request);
            driverDao.createDriver(driver);
            logger.info("Driver created successfully: {}", driver.getId());

            return ResponseStructure.successResponse(driver, "Driver created successfully");

        } catch (Exception e) {
            logger.error("Error Creating driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Driver buildDriverFromRequest(CreateDriverReq request) {
        Driver driver = new Driver();
        driver.setEmail(request.getEmail());
        driver.setPhone(request.getPhone());
        driver.setPassword(encoder.encode(request.getPassword()));
        driver.setUsername((request.getFirstName() + " " + request.getLastName()).toUpperCase());
        driver.setProfilePic(request.getProfilePic());
        driver.setJoiningDate(request.getJoiningDate());
        driver.setAmountPending(request.getAmountPending());
        driver.setTotalOrders(request.getTotalOrders());
        driver.setJahezId(request.getJahezId());
        driver.setVisaExpiryDate(request.getVisaExpiryDate());
        driver.setSalaryAmount(request.getSalaryAmount());
        driver.setAddress(request.getAddress());
        driver.setReferenceLocation(request.getReferenceLocation());
        driver.setVisaType(request.getVisaType());
        driver.setVisaProcurement(request.getVisaProcurement());
        driver.setNationality(request.getNationality());
        driver.setPassportNumber(request.getPassportNumber());
        driver.setCprNumber(request.getCprNumber());
        driver.setVehicleType(request.getVehicleType());
        driver.setLicenceType(request.getLicenceType());
        driver.setLicenceNumber(request.getLicenceNumber());
        driver.setLicenceExpiryDate(request.getLicenceExpiryDate());
        driver.setLicensePhotoUrl(request.getLicensePhotoUrl());
        driver.setRcPhotoUrl(request.getRcPhotoUrl());
        driver.setBankAccountName(request.getBankAccountName());
        driver.setBankName(request.getBankName());
        driver.setBankAccountNumber(request.getBankAccountNumber());
        driver.setBankIbanNumber(request.getBankIbanNumber());
        driver.setBankBranch(request.getBankBranch());
        driver.setBankBranchCode(request.getBankBranchCode());
        driver.setBankSwiftCode(request.getBankSwiftCode());
        driver.setBankIfsc(request.getBankIfsc());
        driver.setBankAccountCurrency(request.getBankAccountCurrency());
        driver.setBankMobilePayNumber(request.getBankMobilePayNumber());
        driver.setPassbookImageUrl(request.getPassbookImageUrl());

        return driver;
    }

    public ResponseEntity<ResponseStructure<Object>> getDriver(Long id) {
        try {
            Driver driver = driverDao.getById(id);
            if (Objects.isNull(driver)) {
                logger.warn("No Driver found. Invalid ID:" + id);
                return ResponseStructure.errorResponse(null, 404, "Invalid Id:" + id);
            }
            return ResponseStructure.successResponse(driver, "Driver found");
        } catch (Exception e) {
            logger.error("Error Creating driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> updateDriver(UpdateDriverReq request) {
        try {
            Driver driver = fetchDriver(request.getId());
            if (Objects.isNull(driver)) {
                logger.warn("No driver found with id:" + request.getId());
                return ResponseStructure.errorResponse(null, 404, "Driver not found with id:" + request.getId());
            }
            Optional<User> userEmail = userDao.getUserByEmail(request.getEmail());

            if (userEmail.isPresent() && !userEmail.get().getId().equals(request.getId())) {
                logger.warn("Email already exists: {}", request.getEmail());
                return ResponseStructure.errorResponse(null, 409, "Email already exists");
            }

            Optional<User> userPhone = userDao.getUserByPhone(request.getPhone());

            if (userPhone.isPresent() && !userPhone.get().getId().equals(request.getId())) {
                logger.warn("Phone already exists: {}", request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Phone already exists");
            }

            driver = updateDriverFromRequest(request, driver);
            driverDao.createDriver(driver);

            logger.info("Driver updated successfully: {}", driver.getId());

            return ResponseStructure.successResponse(driver, "Driver updated successfully");

        } catch (Exception e) {
            logger.error("Error updating driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Driver fetchDriver(Long id) {
        Driver driver = driverDao.getById(id);
        if (Objects.isNull(driver)) {
            logger.warn("No Driver found. Invalid ID:" + id);
            return null;
        }
        return driver;
    }

    private Driver updateDriverFromRequest(UpdateDriverReq request, Driver driver) {
        if (!StringUtil.isEmpty(request.getEmail())) {
            driver.setEmail(request.getEmail());
        }
        if (!StringUtil.isEmpty(request.getPhone())) {
            driver.setPhone(request.getPhone());
        }
        if (!StringUtil.isEmpty(request.getPassword())) {
            driver.setPassword(encoder.encode(request.getPassword()));
        }
        if (!StringUtil.isEmpty(request.getFirstName()) || !StringUtil.isEmpty(request.getLastName())) {
            driver.setUsername((request.getFirstName() + " " + request.getLastName()).toUpperCase());
        }
        if (!StringUtil.isEmpty(request.getProfilePic())) {
            driver.setProfilePic(request.getProfilePic());
        }

        if (!StringUtil.isEmpty(request.getJahezId())) {
            driver.setJahezId(request.getJahezId());
        }
        if (request.getVisaExpiryDate() != null) {
            driver.setVisaExpiryDate(request.getVisaExpiryDate());
        }

        if (!StringUtil.isEmpty(request.getAddress())) {
            driver.setAddress(request.getAddress());
        }
        if (!StringUtil.isEmpty(request.getReferenceLocation())) {
            driver.setReferenceLocation(request.getReferenceLocation());
        }
        if (!StringUtil.isEmpty(request.getVisaType())) {
            driver.setVisaType(request.getVisaType());
        }
        if (!StringUtil.isEmpty(request.getVisaProcurement())) {
            driver.setVisaProcurement(request.getVisaProcurement());
        }
        if (!StringUtil.isEmpty(request.getNationality())) {
            driver.setNationality(request.getNationality());
        }
        if (!StringUtil.isEmpty(request.getPassportNumber())) {
            driver.setPassportNumber(request.getPassportNumber());
        }
        if (!StringUtil.isEmpty(request.getCprNumber())) {
            driver.setCprNumber(request.getCprNumber());
        }
        if (!StringUtil.isEmpty(request.getVehicleType())) {
            driver.setVehicleType(request.getVehicleType());
        }
        if (!StringUtil.isEmpty(request.getLicenceType())) {
            driver.setLicenceType(request.getLicenceType());
        }
        if (!StringUtil.isEmpty(request.getLicenceNumber())) {
            driver.setLicenceNumber(request.getLicenceNumber());
        }
        if (!StringUtil.isEmpty(request.getLicenceExpiryDate())) {
            driver.setLicenceExpiryDate(request.getLicenceExpiryDate());
        }
        if (!StringUtil.isEmpty(request.getLicensePhotoUrl())) {
            driver.setLicensePhotoUrl(request.getLicensePhotoUrl());
        }
        if (!StringUtil.isEmpty(request.getRcPhotoUrl())) {
            driver.setRcPhotoUrl(request.getRcPhotoUrl());
        }
        if (!StringUtil.isEmpty(request.getBankAccountName())) {
            driver.setBankAccountName(request.getBankAccountName());
        }
        if (!StringUtil.isEmpty(request.getBankName())) {
            driver.setBankName(request.getBankName());
        }
        if (!StringUtil.isEmpty(request.getBankAccountNumber())) {
            driver.setBankAccountNumber(request.getBankAccountNumber());
        }
        if (!StringUtil.isEmpty(request.getBankIbanNumber())) {
            driver.setBankIbanNumber(request.getBankIbanNumber());
        }
        if (!StringUtil.isEmpty(request.getBankBranch())) {
            driver.setBankBranch(request.getBankBranch());
        }
        if (!StringUtil.isEmpty(request.getBankBranchCode())) {
            driver.setBankBranchCode(request.getBankBranchCode());
        }
        if (!StringUtil.isEmpty(request.getBankSwiftCode())) {
            driver.setBankSwiftCode(request.getBankSwiftCode());
        }
        if (!StringUtil.isEmpty(request.getBankIfsc())) {
            driver.setBankIfsc(request.getBankIfsc());
        }
        if (!StringUtil.isEmpty(request.getBankAccountCurrency())) {
            driver.setBankAccountCurrency(request.getBankAccountCurrency());
        }
        if (!StringUtil.isEmpty(request.getBankMobilePayNumber())) {
            driver.setBankMobilePayNumber(request.getBankMobilePayNumber());
        }
        if (!StringUtil.isEmpty(request.getPassbookImageUrl())) {
            driver.setPassbookImageUrl(request.getPassbookImageUrl());
        }

        return driver;
    }

    public ResponseEntity<ResponseStructure<Object>> getAllDriver(int page, int size, String field) {
        try {

            Page<Driver> driverPage = driverDao.findAll(page,size,field);
            if (driverPage.isEmpty()) {
                logger.warn("No Admin found.");
                return ResponseStructure.errorResponse(null, 404, "No Driver found");
            }
            return ResponseStructure.successResponse(driverPage, "Driver found");
        } catch (Exception e) {
            logger.error("Error fetching admin", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

}
