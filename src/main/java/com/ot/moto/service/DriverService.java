package com.ot.moto.service;

import com.opencsv.CSVWriter;
import com.ot.moto.dao.*;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.AssetRequest;
import com.ot.moto.dto.request.AssetUpdateReq;
import com.ot.moto.dto.request.CreateDriverReq;
import com.ot.moto.dto.request.UpdateDriverReq;
import com.ot.moto.dto.response.DriverDetails;
import com.ot.moto.dto.response.TopDrivers;
import com.ot.moto.entity.Asset;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.User;
import com.ot.moto.entity.Visa;
import com.ot.moto.repository.AssetRepository;
import com.ot.moto.repository.DriverRepository;
import com.ot.moto.repository.OrdersRepository;
import com.ot.moto.repository.VisaRepository;
import com.ot.moto.util.StringUtil;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;

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

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private VisaRepository visaRepository;

    @Autowired
    private VisaDao visaDao;

    @Autowired
    private AssetsDao assetsDao;

    public ResponseEntity<ResponseStructure<Object>> createDriver(CreateDriverReq request) {
        try {
            // Check for existing user
            if (userDao.checkUserExists(request.getEmail(), request.getPhone())) {
                logger.warn("Email/ Phone already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Email/ Phone already exists");
            }

            // Build the Driver entity from the request
            Driver driver = buildDriverFromRequest(request);

            // Save the Driver entity first
            driver = driverRepository.save(driver);
            logger.info("Driver created successfully: {}", driver.getId());

            Driver savedDriver = driverDao.getById(driver.getId());
            if (Objects.isNull(savedDriver)) {
                logger.warn("No Driver found. Invalid ID:" + driver.getId());
                return ResponseStructure.errorResponse(null, 404, "Invalid Id:" + driver.getId());
            }

            createAssetAndVise(savedDriver, request);

            return ResponseStructure.successResponse(savedDriver, "Driver created successfully");

        } catch (Exception e) {
            logger.error("Error Creating driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private void createAssetAndVise(Driver driver, CreateDriverReq request) {
        // Handle assets after the driver is saved
        if (request.getAssets() != null) {
            List<Asset> assets = new ArrayList<>();
            for (AssetRequest assetRequest : request.getAssets()) {
                Asset asset = new Asset();
                asset.setItem(assetRequest.getItem());
                asset.setQuantity(assetRequest.getQuantity());
                asset.setLocalDate(assetRequest.getLocalDate());
                asset.setDriver(driver); // Link the saved driver to the asset
                assets.add(asset);
            }
            assetsDao.saveAll(assets); // Save all assets after the driver is saved
        }

        Visa visa = visaDao.findById(request.getVisaType());
        if (visa != null) {
            driver.setVisa(visa);
            driverRepository.save(driver);
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
        driver.setDateOfBirth(request.getDateOfBirth());

        driver.setJahezId(request.getJahezId());
        driver.setAmountPending(0.0);
        driver.setAmountReceived(0.0);
        driver.setTotalOrders(0L);
        driver.setCurrentOrders(0L);
        driver.setSalaryAmount(0.0);
        driver.setCodAmount(0.0);
        driver.setBonus(0.0);
        driver.setPayToJahez(0.0);
        driver.setPaidByTam(0.0);
        driver.setProfit(0.0);
        driver.setAddress(request.getAddress());
        driver.setReferenceLocation(request.getReferenceLocation());
        driver.setNationality(request.getNationality());
        driver.setPassportNumber(request.getPassportNumber());
        driver.setPassportExpiryDate(request.getPassportExpiryDate());
        driver.setCprNumber(request.getCprNumber());
        driver.setVehicleType(request.getVehicleType());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setDlType(request.getDlType());
        driver.setDlExpiryDate(request.getDlExpiryDate());
        driver.setBankAccountName(request.getBankAccountName());
        driver.setBankName(request.getBankName());
        driver.setBankAccountNumber(request.getBankAccountNumber());
        driver.setBankIbanNumber(request.getBankIbanNumber());
        driver.setBankBranch(request.getBankBranch());
        driver.setBankBranchCode(request.getBankBranchCode());
        driver.setBankSwiftCode(request.getBankSwiftCode());
        driver.setBankAccountCurrency(request.getBankAccountCurrency());
        driver.setBankMobilePayNumber(request.getBankMobilePayNumber());
        driver.setBankAccountType(request.getBankAccountType());
        driver.setRemarks(request.getRemarks());
        driver.setVisaExpiryDate(request.getVisaExpiryDate());
        // Upload Documents
        driver.setDlFrontPhotoUrl(request.getDlFrontPhotoUrl());
        driver.setDlBackPhotoUrl(request.getDlBackPhotoUrl());
        driver.setRcFrontPhotoUrl(request.getRcFrontPhotoUrl());
        driver.setRcBackPhotoUrl(request.getRcBackPhotoUrl());
        driver.setPassbookImageUrl(request.getPassbookImageUrl());
        driver.setPassportFrontUrl(request.getPassportFrontUrl());
        driver.setPassportBackUrl(request.getPassportBackUrl());
        driver.setCprFrontImageUrl(request.getCprFrontImageUrl());
        driver.setCprBackImageUrl(request.getCprBackImageUrl());
        driver.setCprReaderImageUrl(request.getCprReaderImageUrl());
        driver.setVisaCopyImageUrl(request.getVisaCopyImageUrl());

        // Calculate driver EMI
        calculateDriverEMI(driver, request);

        return driver;
    }

    public void calculateDriverEMI(Driver driver, CreateDriverReq request) {

        driver.setVisaAmount(request.getVisaAmount());
        driver.setVisaAmountStartDate(request.getVisaAmountStartDate());
        driver.setVisaAmountEndDate(request.getVisaAmountEndDate());

        LocalDate startDate = request.getVisaAmountStartDate();
        LocalDate endDate = request.getVisaAmountEndDate();
        Double visaAmount = request.getVisaAmount();

        if (startDate != null && endDate != null && visaAmount != null) {
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 0) {
                double emi = visaAmount / daysBetween;
                driver.setVisaAmountEmi(emi);
            } else {
                throw new RuntimeException("Visa start date must be before end date.");
            }
        } else {
            driver.setVisaAmountEmi(null);
        }

        // Handle Bike Rent EMI calculation
        driver.setBikeRentAmount(request.getBikeRentAmount());
        driver.setBikeRentAmountStartDate(request.getBikeRentAmountStartDate());
        driver.setBikeRentAmountEndDate(request.getBikeRentAmountEndDate());

        LocalDate bikeStartDate = request.getBikeRentAmountStartDate();
        LocalDate bikeEndDate = request.getBikeRentAmountEndDate();
        Double bikeAmount = request.getBikeRentAmount();

        if (bikeStartDate != null && bikeEndDate != null && bikeAmount != null) {
            long bikeDays = ChronoUnit.DAYS.between(bikeStartDate, bikeEndDate);
            if (bikeDays > 0) {
                double emi = bikeAmount / bikeDays;
                driver.setBikeRentAmountEmi(emi);
            } else {
                throw new RuntimeException("Bike rent start date must be before end date.");
            }
        } else {
            driver.setBikeRentAmountEmi(null);
        }

        // Handle Other Deductions EMI calculation
        driver.setOtherDeductionAmount(request.getOtherDeductionAmount());
        driver.setOtherDeductionAmountStartDate(request.getOtherDeductionAmountStartDate());
        driver.setOtherDeductionAmountEndDate(request.getOtherDeductionAmountEndDate());

        LocalDate otherStartDate = request.getOtherDeductionAmountStartDate();
        LocalDate otherEndDate = request.getOtherDeductionAmountEndDate();
        Double otherDeductionAmount = request.getOtherDeductionAmount();

        if (otherStartDate != null && otherEndDate != null && otherDeductionAmount != null) {
            long otherDeductionDays = ChronoUnit.DAYS.between(otherStartDate, otherEndDate);
            if (otherDeductionDays > 0) {
                double emi = otherDeductionAmount / otherDeductionDays;
                driver.setOtherDeductionsAmountEmi(emi);
            } else {
                throw new RuntimeException("Other deductions start date must be before end date.");
            }
        } else {
            driver.setOtherDeductionsAmountEmi(null);
        }
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
            logger.error("Error fetching driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllDriver(int page, int size, String field) {
        try {

            Page<Driver> driverPage = driverDao.findAll(page, size, field);
            if (driverPage.isEmpty()) {
                logger.warn("No Driver found.");
                return ResponseStructure.errorResponse(null, 404, "No Driver found");
            }
            return ResponseStructure.successResponse(driverPage, "All Driver found");
        } catch (Exception e) {
            logger.error("Error fetching Driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public DriverDetails getDriverDetails() {

        long totalDrivers = driverDao.countTotalDrivers();

//        long flexiCount = driverDao.countFlexiVisa();

        /*  long otherVisaTypesCount = driverDao.countOtherVisaTypes();*/

        long ridersCount = driverDao.countTwoWheelerRiders();

        long driversCount = driverDao.countFourWheelerDrivers();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        logger.info("Fetching attendance count for {}", yesterday);
        long attendanceCount = ordersRepository.countDriversWithOrdersOnDate(yesterday);

//        long visaTypeCount = flexiCount + otherVisaTypesCount;

        logger.info("Returning DriverDetails with totalDrivers: {}, attendance: {}, riders: {}, drivers: {}, visaType: {}, flexi: {}",
                totalDrivers, attendanceCount, ridersCount, driversCount);/*, *//*visaTypeCount, flexiCount*/

        return new DriverDetails(totalDrivers, attendanceCount, ridersCount, driversCount);/*, visaTypeCount, flexiCount);*/
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
                responseStructure.setMessage("Driver Not Found With NAME ");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            } else {
                logger.info("Found {} drivers with username containing: {}", driverList.size(), name);
                responseStructure.setStatus(HttpStatus.OK.value());
                responseStructure.setMessage("Driver Found With NAME ");
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

            String[] header = {
                    "Driver ID", "Amount Pending", "Amount Received", "Total Orders", "Current Orders", "Jahez ID",
                    "Visa Expiry Date", "Salary Amount", "Address", "Reference Location", "Nationality", "Passport Number",
                    "CPR Number", "Vehicle Type", "DL Type", "DL Expiry Date", "DL Front Photo URL", "DL Back Photo URL",
                    "RC Front Photo URL", "RC Back Photo URL", "Bank Account Name", "Bank Name", "Bank Account Number",
                    "Bank IBAN Number", "Bank Branch", "Bank Branch Code", "Bank Swift Code", "Bank Account Currency",
                    "Bank Mobile Pay Number", "Visa Amount", "Visa Amount Start Date", "Visa Amount End Date", "Visa Amount EMI",
                    "Bike Rent Amount", "Bike Rent Start Date", "Bike Rent End Date", "Bike Rent EMI", "Other Deduction Amount",
                    "Other Deduction Start Date", "Other Deduction End Date", "Other Deductions EMI", "Remarks"
            };

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
                        driver.getNationality(),
                        driver.getPassportNumber(),
                        driver.getCprNumber(),
                        driver.getVehicleType(),
                        driver.getDlType(),
                        driver.getDlExpiryDate() != null ? driver.getDlExpiryDate().toString() : "",
                        driver.getDlFrontPhotoUrl(),
                        driver.getDlBackPhotoUrl(),
                        driver.getRcFrontPhotoUrl(),
                        driver.getRcBackPhotoUrl(),
                        driver.getBankAccountName(),
                        driver.getBankName(),
                        driver.getBankAccountNumber(),
                        driver.getBankIbanNumber(),
                        driver.getBankBranch(),
                        driver.getBankBranchCode(),
                        driver.getBankSwiftCode(),
                        driver.getBankAccountCurrency(),
                        driver.getBankMobilePayNumber(),
                        driver.getVisaAmount() != null ? String.valueOf(driver.getVisaAmount()) : "",
                        driver.getVisaAmountStartDate() != null ? driver.getVisaAmountStartDate().toString() : "",
                        driver.getVisaAmountEndDate() != null ? driver.getVisaAmountEndDate().toString() : "",
                        driver.getVisaAmountEmi() != null ? String.valueOf(driver.getVisaAmountEmi()) : "",
                        driver.getBikeRentAmount() != null ? String.valueOf(driver.getBikeRentAmount()) : "",
                        driver.getBikeRentAmountStartDate() != null ? driver.getBikeRentAmountStartDate().toString() : "",
                        driver.getBikeRentAmountEndDate() != null ? driver.getBikeRentAmountEndDate().toString() : "",
                        driver.getBikeRentAmountEmi() != null ? String.valueOf(driver.getBikeRentAmountEmi()) : "",
                        driver.getOtherDeductionAmount() != null ? String.valueOf(driver.getOtherDeductionAmount()) : "",
                        driver.getOtherDeductionAmountStartDate() != null ? driver.getOtherDeductionAmountStartDate().toString() : "",
                        driver.getOtherDeductionAmountEndDate() != null ? driver.getOtherDeductionAmountEndDate().toString() : "",
                        driver.getOtherDeductionsAmountEmi() != null ? String.valueOf(driver.getOtherDeductionsAmountEmi()) : "",
                        driver.getRemarks()
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

    public ResponseEntity<ResponseStructure<Object>> countDriversWithFlexiVisa() {
        try {
            Long count = driverDao.countFlexiVisa();
            Long result = (count == null) ? 0L : count;

            logger.info("Successfully retrieved Flexi visa driver count: {}", result);
            return ResponseStructure.successResponse(result, "Driver count retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error while fetching Flexi visa driver count.", e);
            return ResponseStructure.errorResponse(null, 500, "Internal server error.");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> countCrVisa() {
        try {
            Long count = driverDao.countCrVisa();
            Long result = (count == null) ? 0L : count;

            logger.info("Successfully retrieved CR visa driver count: {}", result);
            return ResponseStructure.successResponse(result, "Driver count retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error while fetching CR visa driver count.", e);
            return ResponseStructure.errorResponse(null, 500, "Internal server error.");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> countComapnyVisa() {
        try {
            Long count = driverDao.countCompanyVisa();
            Long result = (count == null) ? 0L : count;

            logger.info("Successfully retrieved Company visa driver count: {}", result);
            return ResponseStructure.successResponse(result, "Driver count retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error while fetching Company visa driver count.", e);
            return ResponseStructure.errorResponse(null, 500, "Internal server error.");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> countOtherVisa() {
        try {
            Long count = driverDao.countOtherVisa();
            Long result = (count == null) ? 0L : count;

            logger.info("Successfully retrieved Other visa driver count: {}", result);
            return ResponseStructure.successResponse(result, "Driver count retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error while fetching Other visa driver count.", e);
            return ResponseStructure.errorResponse(null, 500, "Internal server error.");
        }
    }


    public ResponseEntity<ResponseStructure<Object>> updateDriver(UpdateDriverReq request) {
        try {
            Driver driver = fetchDriver(request.getId());
            if (Objects.isNull(driver)) {
                logger.warn("No driver found with id: {}", request.getId());
                return ResponseStructure.errorResponse(null, 404, "Driver not found with id: " + request.getId());
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
            driverRepository.save(driver);

            if (request.getAssets() != null) {
                updateDriverAssets(driver, request.getAssets());
            }

            if (request.getVisaType() != null) {
                Visa visa = visaDao.findById(request.getVisaType());
                if (visa != null) {
                    driver.setVisa(visa);
                }
            }

            calculateDriverEMI(driver, request);

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
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            driver.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            driver.setPhone(request.getPhone());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            driver.setPassword(encoder.encode(request.getPassword()));
        }
        if (request.getFirstName() != null && !request.getFirstName().isEmpty() || request.getLastName() != null && !request.getLastName().isEmpty()) {
            driver.setUsername((request.getFirstName() + " " + request.getLastName()).toUpperCase());
        }
        if (request.getProfilePic() != null && !request.getProfilePic().isEmpty()) {
            driver.setProfilePic(request.getProfilePic());
        }

        // Update date fields only if provided
        if (request.getJoiningDate() != null) {
            driver.setJoiningDate(request.getJoiningDate());
        }
        if (request.getDateOfBirth() != null) {
            driver.setDateOfBirth(request.getDateOfBirth());
        }

        // Update other fields only if provided
        if (request.getJahezId() != null && !request.getJahezId().isEmpty()) {
            driver.setJahezId(request.getJahezId());
        }
        if (request.getVisaExpiryDate() != null) {
            driver.setVisaExpiryDate(request.getVisaExpiryDate());
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            driver.setAddress(request.getAddress());
        }
        if (request.getReferenceLocation() != null && !request.getReferenceLocation().isEmpty()) {
            driver.setReferenceLocation(request.getReferenceLocation());
        }
        if (request.getNationality() != null && !request.getNationality().isEmpty()) {
            driver.setNationality(request.getNationality());
        }
        if (request.getPassportNumber() != null && !request.getPassportNumber().isEmpty()) {
            driver.setPassportNumber(request.getPassportNumber());
        }
        if (request.getPassportExpiryDate() != null) {
            driver.setPassportExpiryDate(request.getPassportExpiryDate());
        }
        if (request.getCprNumber() != null && !request.getCprNumber().isEmpty()) {
            driver.setCprNumber(request.getCprNumber());
        }
        if (request.getVehicleType() != null && !request.getVehicleType().isEmpty()) {
            driver.setVehicleType(request.getVehicleType());
        }
        if (request.getVehicleNumber() != null && !request.getVehicleNumber().isEmpty()) {
            driver.setVehicleNumber(request.getVehicleNumber());
        }
        if (request.getDlType() != null && !request.getDlType().isEmpty()) {
            driver.setDlType(request.getDlType());
        }
        if (request.getDlExpiryDate() != null) {
            driver.setDlExpiryDate(request.getDlExpiryDate());
        }
        if (request.getBankAccountName() != null && !request.getBankAccountName().isEmpty()) {
            driver.setBankAccountName(request.getBankAccountName());
        }
        if (request.getBankName() != null && !request.getBankName().isEmpty()) {
            driver.setBankName(request.getBankName());
        }
        if (request.getBankAccountNumber() != null && !request.getBankAccountNumber().isEmpty()) {
            driver.setBankAccountNumber(request.getBankAccountNumber());
        }
        if (request.getBankIbanNumber() != null && !request.getBankIbanNumber().isEmpty()) {
            driver.setBankIbanNumber(request.getBankIbanNumber());
        }
        if (request.getBankBranch() != null && !request.getBankBranch().isEmpty()) {
            driver.setBankBranch(request.getBankBranch());
        }
        if (request.getBankBranchCode() != null && !request.getBankBranchCode().isEmpty()) {
            driver.setBankBranchCode(request.getBankBranchCode());
        }
        if (request.getBankSwiftCode() != null && !request.getBankSwiftCode().isEmpty()) {
            driver.setBankSwiftCode(request.getBankSwiftCode());
        }
        if (request.getBankAccountCurrency() != null && !request.getBankAccountCurrency().isEmpty()) {
            driver.setBankAccountCurrency(request.getBankAccountCurrency());
        }
        if (request.getBankMobilePayNumber() != null && !request.getBankMobilePayNumber().isEmpty()) {
            driver.setBankMobilePayNumber(request.getBankMobilePayNumber());
        }
        if (request.getBankAccountType() != null && !request.getBankAccountType().isEmpty()) {
            driver.setBankAccountType(request.getBankAccountType());
        }

        // Update document URLs
        if (request.getDlFrontPhotoUrl() != null && !request.getDlFrontPhotoUrl().isEmpty()) {
            driver.setDlFrontPhotoUrl(request.getDlFrontPhotoUrl());
        }
        if (request.getDlBackPhotoUrl() != null && !request.getDlBackPhotoUrl().isEmpty()) {
            driver.setDlBackPhotoUrl(request.getDlBackPhotoUrl());
        }
        if (request.getRcFrontPhotoUrl() != null && !request.getRcFrontPhotoUrl().isEmpty()) {
            driver.setRcFrontPhotoUrl(request.getRcFrontPhotoUrl());
        }
        if (request.getRcBackPhotoUrl() != null && !request.getRcBackPhotoUrl().isEmpty()) {
            driver.setRcBackPhotoUrl(request.getRcBackPhotoUrl());
        }
        if (request.getPassbookImageUrl() != null && !request.getPassbookImageUrl().isEmpty()) {
            driver.setPassbookImageUrl(request.getPassbookImageUrl());
        }
        if (request.getPassportFrontUrl() != null && !request.getPassportFrontUrl().isEmpty()) {
            driver.setPassportFrontUrl(request.getPassportFrontUrl());
        }
        if (request.getPassportBackUrl() != null && !request.getPassportBackUrl().isEmpty()) {
            driver.setPassportBackUrl(request.getPassportBackUrl());
        }
        if (request.getCprFrontImageUrl() != null && !request.getCprFrontImageUrl().isEmpty()) {
            driver.setCprFrontImageUrl(request.getCprFrontImageUrl());
        }
        if (request.getCprBackImageUrl() != null && !request.getCprBackImageUrl().isEmpty()) {
            driver.setCprBackImageUrl(request.getCprBackImageUrl());
        }
        if (request.getCprReaderImageUrl() != null && !request.getCprReaderImageUrl().isEmpty()) {
            driver.setCprReaderImageUrl(request.getCprReaderImageUrl());
        }
        if (request.getVisaCopyImageUrl() != null && !request.getVisaCopyImageUrl().isEmpty()) {
            driver.setVisaCopyImageUrl(request.getVisaCopyImageUrl());
        }

        return driver;
    }


    private void updateDriverAssets(Driver driver, List<AssetUpdateReq> assetUpdateReqs) {
        // Fetch existing assets for the driver
        List<Asset> existingAssets = assetsDao.findByDriver(driver);

        // Collect asset IDs to keep from the incoming requests
        Set<Long> assetIdsToKeep = assetUpdateReqs.stream()
                .map(AssetUpdateReq::getId) // Use AssetUpdateReq to get ID
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Remove assets that are no longer in the request
        existingAssets.removeIf(asset -> !assetIdsToKeep.contains(asset.getId()));
        if (!existingAssets.isEmpty()) {
            assetsDao.deleteAll(existingAssets);
        }

        // Add or update assets based on the incoming requests
        for (AssetUpdateReq assetUpdateReq : assetUpdateReqs) {
            Asset asset;
            if (assetUpdateReq.getId() != null) {
                asset = assetsDao.findById(assetUpdateReq.getId());
                if (asset == null) {
                    asset = new Asset(); // Create a new asset if not found
                }
            } else {
                asset = new Asset(); // Create a new asset if no ID is provided
            }

            // Update asset details from the request
            asset.setItem(assetUpdateReq.getItem());
            asset.setQuantity(assetUpdateReq.getQuantity());
            asset.setLocalDate(assetUpdateReq.getLocalDate());
            asset.setDriver(driver);

            // Save or update the asset
            assetsDao.save(asset);
        }
    }

    private void calculateDriverEMI(Driver driver, UpdateDriverReq request) {
        driver.setVisaAmount(request.getVisaAmount());
        driver.setVisaAmountStartDate(request.getVisaAmountStartDate());
        driver.setVisaAmountEndDate(request.getVisaAmountEndDate());

        LocalDate visaStartDate = request.getVisaAmountStartDate();
        LocalDate visaEndDate = request.getVisaAmountEndDate();
        Double visaAmount = request.getVisaAmount();

        if (visaStartDate != null && visaEndDate != null && visaAmount != null) {
            long daysBetween = ChronoUnit.DAYS.between(visaStartDate, visaEndDate);
            if (daysBetween > 0) {
                double emi = visaAmount / daysBetween;
                driver.setVisaAmountEmi(emi);
            } else {
                throw new RuntimeException("Visa start date must be before end date.");
            }
        } else {
            driver.setVisaAmountEmi(null);
        }

        // Bike Rent EMI Calculation
        driver.setBikeRentAmount(request.getBikeRentAmount());
        driver.setBikeRentAmountStartDate(request.getBikeRentAmountStartDate());
        driver.setBikeRentAmountEndDate(request.getBikeRentAmountEndDate());

        LocalDate bikeStartDate = request.getBikeRentAmountStartDate();
        LocalDate bikeEndDate = request.getBikeRentAmountEndDate();
        Double bikeAmount = request.getBikeRentAmount();

        if (bikeStartDate != null && bikeEndDate != null && bikeAmount != null) {
            long bikeDays = ChronoUnit.DAYS.between(bikeStartDate, bikeEndDate);
            if (bikeDays > 0) {
                double emi = bikeAmount / bikeDays;
                driver.setBikeRentAmountEmi(emi);
            } else {
                throw new RuntimeException("Bike rent start date must be before end date.");
            }
        } else {
            driver.setBikeRentAmountEmi(null);
        }

        // Other Deductions EMI Calculation
        driver.setOtherDeductionAmount(request.getOtherDeductionAmount());
        driver.setOtherDeductionAmountStartDate(request.getOtherDeductionAmountStartDate());
        driver.setOtherDeductionAmountEndDate(request.getOtherDeductionAmountEndDate());

        LocalDate otherStartDate = request.getOtherDeductionAmountStartDate();
        LocalDate otherEndDate = request.getOtherDeductionAmountEndDate();
        Double otherDeductionAmount = request.getOtherDeductionAmount();

        if (otherStartDate != null && otherEndDate != null && otherDeductionAmount != null) {
            long otherDeductionDays = ChronoUnit.DAYS.between(otherStartDate, otherEndDate);
            if (otherDeductionDays > 0) {
                double emi = otherDeductionAmount / otherDeductionDays;
                driver.setOtherDeductionsAmountEmi(emi);
            } else {
                throw new RuntimeException("Other deductions start date must be before end date.");
            }
        } else {
            driver.setOtherDeductionsAmountEmi(null);
        }
    }

    public Long getCountOfOwnedVehicleDrivers() {
        return driverDao.countOwnedVehicleDrivers();
    }
}