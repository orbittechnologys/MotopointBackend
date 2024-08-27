package com.ot.moto.service;

import com.opencsv.CSVWriter;
import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.OrderDao;
import com.ot.moto.dao.UserDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateDriverReq;
import com.ot.moto.dto.request.UpdateDriverReq;
import com.ot.moto.dto.response.DriverDetails;
import com.ot.moto.dto.response.TopDrivers;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.User;
import com.ot.moto.repository.DriverRepository;
import com.ot.moto.repository.OrdersRepository;
import com.ot.moto.util.StringUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private DriverRepository driverRepository;


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

            Page<Driver> driverPage = driverDao.findAll(page, size, field);
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

    public DriverDetails getDriverDetails() {

        long totalDrivers = driverDao.countTotalDrivers();

        long flexiCount = driverDao.countFlexiVisa();

        long otherVisaTypesCount = driverDao.countOtherVisaTypes();

        long ridersCount = driverDao.countTwoWheelerRiders();

        long driversCount = driverDao.countFourWheelerDrivers();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        logger.info("Fetching attendance count for {}", yesterday);
        long attendanceCount = ordersRepository.countDriversWithOrdersOnDate(yesterday);

        long visaTypeCount = flexiCount + otherVisaTypesCount;

        logger.info("Returning DriverDetails with totalDrivers: {}, attendance: {}, riders: {}, drivers: {}, visaType: {}, flexi: {}",
                totalDrivers, attendanceCount, ridersCount, driversCount, visaTypeCount, flexiCount);

        return new DriverDetails(totalDrivers, attendanceCount, ridersCount, driversCount, visaTypeCount, flexiCount);
    }

    public ResponseEntity<ResponseStructure<Object>> deleteDriver(Long driverId) {
        try {
            Driver driver = driverDao.getById(driverId);
            if (Objects.isNull(driver)) {
                logger.warn("Driver not found with ID: {}", driverId);
                return ResponseStructure.errorResponse(null, 404, "Driver not found");
            }

            driverDao.deleteDriver(driver);
            logger.info("Driver deleted successfully: {}", driverId);
            return ResponseStructure.successResponse(null, "Driver deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting Driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> fetchTopDriver() {
        try {
            logger.info("Fetching top drivers...");

            Driver topDriverTotalOrders = driverDao.findTopDriverByTotalOrders();
            Driver topDriverCurrentOrders = driverDao.findTopDriversByCurrentOrders();

            TopDrivers topDrivers = new TopDrivers(topDriverTotalOrders, topDriverCurrentOrders);

            logger.info("Successfully fetched top drivers.");

            return ResponseStructure.successResponse(topDrivers, "Fetched top drivers");
        } catch (Exception e) {
            logger.error("Error fetching top drivers", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<List<Driver>>> findByUsernameContaining(String name) {
        ResponseStructure<List<Driver>> responseStructure = new ResponseStructure<>();
        try {
            logger.info("Searching for drivers with username containing: {}", name);

            List<Driver> driverList = driverDao.findByUsernameContaining(name);
            if (driverList.isEmpty()) {
                logger.warn("No drivers found with username containing: {}", name);
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("Driver Not Found With NAME");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            } else {
                logger.info("Found {} drivers with username containing: {}", driverList.size(), name);
                responseStructure.setStatus(HttpStatus.OK.value());
                responseStructure.setMessage("Driver Found With NAME");
                responseStructure.setData(driverList);
                return new ResponseEntity<>(responseStructure, HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error("Error searching for drivers with username containing: {}", name, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<InputStreamResource> generateCsvForDrivers() {
        try {
            List<Driver> allDrivers = driverRepository.findAll();
            if (allDrivers.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            String[] header = {"Driver ID", "Amount Pending", "Amount Received", "Total Orders", "Current Orders", "Jahez ID",
                    "Visa Expiry Date", "Salary Amount", "Address", "Reference Location", "Visa Type", "Visa Procurement",
                    "Nationality", "Passport Number", "CPR Number", "Vehicle Type", "Licence Type", "Licence Number",
                    "Licence Expiry Date", "License Photo URL", "RC Photo URL", "Bank Account Name", "Bank Name",
                    "Bank Account Number", "Bank IBAN Number", "Bank Branch", "Bank Branch Code", "Bank Swift Code",
                    "Bank IFSC", "Bank Account Currency", "Bank Mobile Pay Number", "Passbook Image URL"};

            csvWriter.writeNext(header);

            for (Driver driver : allDrivers) {
                String[] data = {
                        String.valueOf(driver.getId()),
                        String.valueOf(driver.getAmountPending()),
                        String.valueOf(driver.getAmountReceived()),
                        String.valueOf(driver.getTotalOrders()),
                        String.valueOf(driver.getCurrentOrders()),
                        driver.getJahezId(),
                        driver.getVisaExpiryDate() != null ? driver.getVisaExpiryDate().toString() : "",
                        String.valueOf(driver.getSalaryAmount()),
                        driver.getAddress(),
                        driver.getReferenceLocation(),
                        driver.getVisaType(),
                        driver.getVisaProcurement(),
                        driver.getNationality(),
                        driver.getPassportNumber(),
                        driver.getCprNumber(),
                        driver.getVehicleType(),
                        driver.getLicenceType(),
                        driver.getLicenceNumber(),
                        driver.getLicenceExpiryDate(),
                        driver.getLicensePhotoUrl(),
                        driver.getRcPhotoUrl(),
                        driver.getBankAccountName(),
                        driver.getBankName(),
                        driver.getBankAccountNumber(),
                        driver.getBankIbanNumber(),
                        driver.getBankBranch(),
                        driver.getBankBranchCode(),
                        driver.getBankSwiftCode(),
                        driver.getBankIfsc(),
                        driver.getBankAccountCurrency(),
                        driver.getBankMobilePayNumber(),
                        driver.getPassbookImageUrl()
                };
                csvWriter.writeNext(data);
            }

            csvWriter.close();
            String csvContent = writer.toString();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(csvContent.getBytes());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=drivers.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(csvContent.getBytes().length)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(resource);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseStructure<Map<String, Object>> getDriverAttendanceDetails() {
        try {

            List<Driver> allDrivers = driverDao.getAllDrivers();

            List<Driver> todayDrivers = orderDao.getDriversWithOrdersForToday();

            List<Object[]> monthlyResults = orderDao.getDriverAttendanceForCurrentMonth();
            Map<Long, Long> driverDaysPresentMap = monthlyResults.stream()
                    .collect(Collectors.toMap(
                            result -> ((Driver) result[0]).getId(),
                            result -> (Long) result[1]
                    ));

            List<Map<String, Object>> driverAttendanceList = allDrivers.stream().map(driver -> {
                Map<String, Object> driverData = new HashMap<>();
                driverData.put("driverId", driver.getId());
                driverData.put("driverName", driver.getUsername());
                driverData.put("driverProfilePic", driver.getProfilePic());
                driverData.put("daysPresent", driverDaysPresentMap.getOrDefault(driver.getId(), 0L));
                driverData.put("attendance", todayDrivers.contains(driver) ? "Present" : "Absent");
                return driverData;
            }).collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("totalWorkingDaysInMonth", 24);  // You can adjust this if needed
            data.put("driverCount", driverAttendanceList.size());
            data.put("drivers", driverAttendanceList);

            ResponseStructure<Map<String, Object>> response = new ResponseStructure<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Driver attendance details fetched successfully");
            response.setData(data);
            return response;
        } catch (Exception e) {
            ResponseStructure<Map<String, Object>> response = new ResponseStructure<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Error fetching driver attendance details: " + e.getMessage());
            response.setData(null);
            return response;
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getSumPayToJahezForAllDrivers() {
        try {
            Double amount = driverDao.SumPayToJahezForAllDrivers();
            Double result = (amount == null) ? 0 : amount;

            logger.info("Successfully retrieved sumPayToJahez for all drivers: {}", result);
            return ResponseStructure.successResponse(result, "Sum retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error while fetching sumPayToJahez for all drivers.", e);
            return ResponseStructure.errorResponse(null, 500, "Internal server error.");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getSumProfitForAllDrivers() {
        try {
            Double amount = driverDao.sumProfitForAllDrivers();
            Double result = (amount == null) ? 0 : amount;

            logger.info("Successfully retrieved sumProfit for all drivers: {}", result);
            return ResponseStructure.successResponse(result, "Sum retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error while fetching sumProfit for all drivers.", e);
            return ResponseStructure.errorResponse(null, 500, "Internal server error.");
        }
    }

    public ResponseEntity<InputStreamResource> generateCsvForDriversForSummary() {
        try {
            List<Driver> allDrivers = driverRepository.findAll();
            if (allDrivers.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Create an output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            CSVWriter csvWriter = new CSVWriter(writer);

            // Define the header
            String[] header = {"Driver Name", "Deliveries", "Salary", "Bonus", "Pay To Jahez", "Paid By Tam", "Profit"};
            csvWriter.writeNext(header);

            // Populate the data rows
            for (Driver driver : allDrivers) {
                String[] data = {
                        driver.getUsername(), // Accessing username from the User superclass
                        driver.getOrders() != null ? String.valueOf(driver.getOrders().size()) : "0", // Assuming deliveries means the number of orders
                        driver.getSalaryAmount() != null ? String.valueOf(driver.getSalaryAmount()) : "0",
                        driver.getBonus() != null ? String.valueOf(driver.getBonus()) : "0",
                        driver.getPayToJahez() != null ? String.valueOf(driver.getPayToJahez()) : "0",
                        driver.getPaidByTam() != null ? String.valueOf(driver.getPaidByTam()) : "0",
                        driver.getProfit() != null ? String.valueOf(driver.getProfit()) : "0"
                };
                csvWriter.writeNext(data);
            }

            // Close the writer
            csvWriter.close();
            writer.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=drivers.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(outputStream.toByteArray().length)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
