package com.ot.moto.service;

import com.opencsv.CSVWriter;
import com.ot.moto.dao.*;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.*;
import com.ot.moto.dto.response.DriverAnalysisSum;
import com.ot.moto.dto.response.DriverDetails;
import com.ot.moto.dto.response.TopDrivers;
import com.ot.moto.entity.*;
import com.ot.moto.repository.*;
import jakarta.transaction.Transactional;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DriverService {

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

    @Autowired
    private PenaltyDao penaltyDao;

    @Autowired
    private OtherDeductionRepository otherDeductionRepository;

    @Autowired
    private FleetDao fleetDao;

    @Autowired
    private FleetRepository fleetRepository;

    @Autowired
    private FleetHistoryRepository fleetHistoryRepository;

    @Autowired
    private OtherDeductionDao otherDeductionDao;

    @Autowired
    private FleetHistoryDao fleetHistoryDao;

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    public ResponseEntity<ResponseStructure<Object>> createDriver(CreateDriverReq request) {
        try {
            // Check for existing user
            if (userDao.checkUserExists(request.getPhone())) {
                logger.warn("Phone number already exists: {}", request.getPhone());
                return ResponseStructure.errorResponse(null, 409, " Phone number " + request.getPhone() + " already exists.");
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
            createOtherDeductionForDriver(savedDriver, request);

            return ResponseStructure.successResponse(savedDriver, "Driver created successfully");

        } catch (Exception e) {
            logger.error("Error Creating driver,please check all the fields properly", e);
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
        driver.setDateOfBirth(request.getDateOfBirth());

        driver.setJahezId(request.getJahezId());
        driver.setAmountPending(0.0);
        driver.setAmountReceived(0.0);
        driver.setTotalOrders(0L);
        driver.setCurrentOrders(0L);

        driver.setCodAmount(0.0);
        driver.setBonus(0.0);
        driver.setPayToJahez(0.0);
        driver.setPaidByTam(0.0);

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
        driver.setVisaExpiryDate(request.getVisaExpiryDate());

        driver.setRemarks(request.getRemarks());
//        driver.setDeductionDescription(request.getDeductionDescription());
        /*        driver.setConsentDoc(request.getConsentDoc());*/

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

    private void createOtherDeductionForDriver(Driver driver, CreateDriverReq request) {
        if (request.getOtherDeduction() != null) {
            List<OtherDeduction> otherDeductions = new ArrayList<>();
            for (CreateOtherDeductionRequest otherDeductionRequest : request.getOtherDeduction()) {
                OtherDeduction otherDeduction = new OtherDeduction();
                otherDeduction.setOtherDeductionAmount(otherDeductionRequest.getOtherDeductionAmount());
                otherDeduction.setOtherDeductionDescription(otherDeductionRequest.getOtherDeductionDescription());
                otherDeduction.setOtherDeductionAmountStartDate(otherDeductionRequest.getOtherDeductionAmountStartDate());
                otherDeduction.setOtherDeductionAmountEndDate(otherDeductionRequest.getOtherDeductionAmountEndDate());
                LocalDate startDate = otherDeductionRequest.getOtherDeductionAmountStartDate();
                LocalDate endDate = otherDeductionRequest.getOtherDeductionAmountEndDate();
                Double otherDeductionAmount = otherDeductionRequest.getOtherDeductionAmount();
                otherDeduction.setDriver(driver);
                if (startDate != null && endDate != null && otherDeductionAmount != null) {
                    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    if (daysBetween > 0) {
                        double emi = otherDeductionAmount / daysBetween;
                        otherDeduction.setOtherDeductionAmountEmi(emi);
                    } else {
                        throw new RuntimeException("date must be before end date.");
                    }
                } else {
                    otherDeduction.setOtherDeductionAmountEmi(null);
                    otherDeduction.setDriver(null);
                }
                otherDeductions.add(otherDeduction);
            }
            otherDeductionRepository.saveAll(otherDeductions);
        }
    }

    public void calculateDriverEMI(Driver driver, CreateDriverReq request) {

        driver.setVisaAmount(request.getVisaAmount());
        driver.setVisaAmountStartDate(request.getVisaAmountStartDate());
        driver.setVisaAmountEndDate(request.getVisaAmountEndDate());

        LocalDate startDate = request.getVisaAmountStartDate();
        LocalDate endDate = request.getVisaAmountEndDate();
        Double visaAmount = request.getVisaAmount();

        if ( visaAmount != null) {
            long daysBetween = 30;
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

        if (bikeAmount != null) {
            long bikeDays = 30;
            if (bikeDays > 0) {
                double emi = bikeAmount / bikeDays;
                driver.setBikeRentAmountEmi(emi);
            } else {
                throw new RuntimeException("Bike rent start date must be before end date.");
            }
        } else {
            driver.setBikeRentAmountEmi(null);
        }
    }

    public ResponseEntity<ResponseStructure<Object>> updateDriver(UpdateDriverReq request) {
        try {
            Driver driver = fetchDriver(request.getId());
            if (Objects.isNull(driver)) {
                logger.warn("No driver found with id: {}", request.getId());
                return ResponseStructure.errorResponse(null, 404, "Driver not found with id: " + request.getId());
            }

            Optional<User> userPhone = userDao.getUserByPhone(request.getPhone());
            if (userPhone.isPresent() && !userPhone.get().getId().equals(request.getId())) {
                logger.warn("Phone already exists: {}", request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Phone already exists");
            }

            driver = updateDriverFromRequest(request, driver);
            calculateDriverEMI(driver, request);
            Driver updatedDriver = driverRepository.save(driver);

            if (request.getAssets() != null) {
                updateDriverAssetsV2(updatedDriver, request);
            }

            if (request.getVisaType() != null) {
                Visa visa = visaDao.findById(request.getVisaType());
                if (visa != null) {
                    driver.setVisa(visa);
                }
            }

            if (request.getOtherDeduction() != null) {
                updateOtherDeductionsForDriverV2(updatedDriver, request);
            }

            logger.info("Driver updated successfully: {}", driver.getId());
            return ResponseStructure.successResponse(driver, "Driver updated successfully");
        } catch (Exception e) {
            logger.error("Error updating driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
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
        logger.info("Updating driver with ID {}. Current username: {}", driver.getId(), driver.getUsername());
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            String newUsername = request.getUsername().toUpperCase();
            logger.info("Updating username from {} to {}", driver.getUsername(), newUsername);
            driver.setUsername(newUsername);
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
        if (request.getJahezId() != null) {
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

        if (request.getRemarks() != null && !request.getRemarks().isEmpty()) {
            driver.setRemarks(request.getRemarks());
        }
        if (request.getConsentDoc() != null && !request.getConsentDoc().isEmpty()) {
            driver.setConsentDoc(request.getConsentDoc());
        }

        return driver;
    }

    private void updateDriverAssetsV2(Driver driver, UpdateDriverReq request) {
        if (request.getAssets() != null) {
            List<Asset> assets = new ArrayList<>();
            for (AssetUpdateReq assetUpdateReq : request.getAssets()) {
                Asset asset;

                // If ID is provided, try to find the existing Asset, otherwise create a new one
                if (assetUpdateReq.getId() != null) {
                    asset = assetsDao.findById(assetUpdateReq.getId());
                } else {
                    asset = new Asset(); // Create a new instance if ID is null
                }
                // If the OtherDeduction exists or is newly created, update its details
                if (asset != null) {
                    asset.setItem(assetUpdateReq.getItem());
                    asset.setQuantity(assetUpdateReq.getQuantity());
                    asset.setLocalDate(assetUpdateReq.getLocalDate());
                    asset.setDriver(driver); // Set the driver for this deduction
                    assets.add(asset);
                } else {
                    throw new RuntimeException("Asset with ID " + assetUpdateReq.getId() + " not found.");
                }
            }
            assetsDao.saveAll(assets);
        }

    }

    /*private void updateOtherDeductionsForDriverV2(Driver driver, UpdateDriverReq request) {
        if (request.getOtherDeduction() != null) {
            List<OtherDeduction> existingDeductions = otherDeductionDao.findByDriverId(driver.getId());
            List<OtherDeduction> deductionsToSave = new ArrayList<>();

            // Create a map for quick lookup of existing deductions
            Map<Long, OtherDeduction> existingDeductionMap = existingDeductions.stream()
                    .collect(Collectors.toMap(OtherDeduction::getId, Function.identity()));

            for (UpdateOtherDeductionReq updateDeductionReq : request.getOtherDeduction()) {
                OtherDeduction otherDeduction;

                // If ID is provided, try to find the existing OtherDeduction
                if (updateDeductionReq.getId() != null) {
                    otherDeduction = existingDeductionMap.get(updateDeductionReq.getId());
                    if (otherDeduction == null) {
                        // If not found, create a new deduction
                        otherDeduction = new OtherDeduction();
                    }
                } else {
                    otherDeduction = new OtherDeduction(); // Create a new instance if ID is null
                }

                // Update otherDeduction properties
                otherDeduction.setOtherDeductionAmount(updateDeductionReq.getOtherDeductionAmount());
                otherDeduction.setOtherDeductionDescription(updateDeductionReq.getOtherDeductionDescription());
                otherDeduction.setOtherDeductionAmountStartDate(updateDeductionReq.getOtherDeductionAmountStartDate());
                otherDeduction.setOtherDeductionAmountEndDate(updateDeductionReq.getOtherDeductionAmountEndDate());
                otherDeduction.setDriver(driver); // Set the driver for this deduction

                LocalDate startDate = updateDeductionReq.getOtherDeductionAmountStartDate();
                LocalDate endDate = updateDeductionReq.getOtherDeductionAmountEndDate();
                Double otherDeductionAmount = updateDeductionReq.getOtherDeductionAmount();

                // Calculate EMI if start and end dates and amount are provided
                if (startDate != null && endDate != null && otherDeductionAmount != null) {
                    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
                    if (daysBetween > 0) {
                        double emi = otherDeductionAmount / daysBetween;
                        otherDeduction.setOtherDeductionAmountEmi(emi);
                    } else {
                        throw new RuntimeException("Start date must be before end date.");
                    }
                }

                // Add to the list of deductions to save (whether updated or newly created)
                deductionsToSave.add(otherDeduction);
            }
            // Save or update the deductions to the repository
            otherDeductionRepository.saveAll(deductionsToSave);
        }
    }*/

    private void updateOtherDeductionsForDriverV2(Driver driver, UpdateDriverReq request) {
        if (request.getOtherDeduction() != null) {
            List<OtherDeduction> deductions = new ArrayList<>();

            for (UpdateOtherDeductionReq deductionReq : request.getOtherDeduction()) {
                OtherDeduction otherDeduction;

                // If ID is provided, try to find the existing OtherDeduction, otherwise create a new one
                if (deductionReq.getId() != null) {
                    otherDeduction = otherDeductionDao.findById(deductionReq.getId());
                } else {
                    otherDeduction = new OtherDeduction(); // Create a new instance if ID is null
                }

                // Update otherDeduction properties
                otherDeduction.setOtherDeductionAmount(deductionReq.getOtherDeductionAmount());
                otherDeduction.setOtherDeductionDescription(deductionReq.getOtherDeductionDescription());
                otherDeduction.setOtherDeductionAmountStartDate(deductionReq.getOtherDeductionAmountStartDate());
                otherDeduction.setOtherDeductionAmountEndDate(deductionReq.getOtherDeductionAmountEndDate());
                otherDeduction.setDriver(driver); // Set the driver for this deduction

                LocalDate startDate = deductionReq.getOtherDeductionAmountStartDate();
                LocalDate endDate = deductionReq.getOtherDeductionAmountEndDate();
                Double otherDeductionAmount = deductionReq.getOtherDeductionAmount();

                // Calculate EMI if start and end dates and amount are provided
                if (startDate != null && endDate != null && otherDeductionAmount != null) {
                    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    if (daysBetween > 0) {
                        double emi = otherDeductionAmount / daysBetween;
                        otherDeduction.setOtherDeductionAmountEmi(emi);
                    } else {
                        throw new RuntimeException("Start date must be before end date.");
                    }
                }

                // Add to the list of deductions to be saved or updated
                deductions.add(otherDeduction);
            }

            // Save all the deductions (either new or updated)
            otherDeductionRepository.saveAll(deductions);
        }
    }

    private void calculateDriverEMI(Driver driver, UpdateDriverReq request) {
        // Visa EMI Calculation
        if (request.getVisaAmount() != null) {
            driver.setVisaAmount(request.getVisaAmount());
        }
        if (request.getVisaAmountStartDate() != null) {
            driver.setVisaAmountStartDate(request.getVisaAmountStartDate());
        }
        if (request.getVisaAmountEndDate() != null) {
            driver.setVisaAmountEndDate(request.getVisaAmountEndDate());
        }

        LocalDate visaStartDate = request.getVisaAmountStartDate();
        LocalDate visaEndDate = request.getVisaAmountEndDate();
        Double visaAmount = request.getVisaAmount();

        if (visaAmount != null) {
            long daysBetween = 30;
            if (daysBetween > 0) {
                double emi = visaAmount / daysBetween;
                driver.setVisaAmountEmi(emi);
            } else {
                throw new RuntimeException("Visa start date must be before end date.");
            }
        } else if (request.getVisaAmount() == null ) {
            // If any required field is missing, keep the old EMI value
            driver.setVisaAmountEmi(driver.getVisaAmountEmi());
        }

        // Bike Rent EMI Calculation
        if (request.getBikeRentAmount() != null) {
            driver.setBikeRentAmount(request.getBikeRentAmount());
        }
        if (request.getBikeRentAmountStartDate() != null) {
            driver.setBikeRentAmountStartDate(request.getBikeRentAmountStartDate());
        }
        if (request.getBikeRentAmountEndDate() != null) {
            driver.setBikeRentAmountEndDate(request.getBikeRentAmountEndDate());
        }

        LocalDate bikeStartDate = request.getBikeRentAmountStartDate();
        LocalDate bikeEndDate = request.getBikeRentAmountEndDate();
        Double bikeAmount = request.getBikeRentAmount();

        if ( bikeAmount != null) {
            long bikeDays = 30;
            if (bikeDays > 0) {
                double emi = bikeAmount / bikeDays;
                driver.setBikeRentAmountEmi(emi);
            } else {
                throw new RuntimeException("Bike rent start date must be before end date.");
            }
        } else if (request.getBikeRentAmount() == null ) {
            // If any required field is missing, keep the old EMI value
            driver.setBikeRentAmountEmi(driver.getBikeRentAmountEmi());
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
            return ResponseStructure.successResponse(driverPage, "All Drivers found");
        } catch (Exception e) {
            logger.error("Error fetching Drivers", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public DriverDetails getDriverDetails() {

        long totalDrivers = driverDao.countTotalDrivers();
        long ridersCount = driverDao.countTwoWheelerRiders();
        long driversCount = driverDao.countFourWheelerDrivers();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        logger.info("Fetching attendance count for {}", yesterday);
        long attendanceCount = ordersRepository.countDriversWithOrdersOnDate(yesterday);

        logger.info("Returning DriverDetails with totalDrivers: {}, attendance: {}, riders: {}, drivers: {}, visaType: {}, flexi: {}",
                totalDrivers, attendanceCount, ridersCount, driversCount);/*, *//*visaTypeCount, flexiCount*/

        return new DriverDetails(totalDrivers, attendanceCount, ridersCount, driversCount);/*, visaTypeCount, flexiCount);*/
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
                    "Bike Rent Amount", "Bike Rent Start Date", "Bike Rent End Date", "Bike Rent EMI",
                    "Other Deduction Amount(s)", "Other Deduction Start Date(s)", "Other Deduction End Date(s)",
                    "Other Deductions EMI(s)", "Orders Info", "Assets Names", "Visa Name", "Remarks"
            };

            csvWriter.writeNext(header);

            for (Driver driver : allDrivers) {
                // Other Deductions
                String otherDeductionAmounts = driver.getOtherDeductions() != null ?
                        driver.getOtherDeductions().stream()
                                .map(deduction -> deduction.getOtherDeductionAmount() != null ?
                                        String.valueOf(deduction.getOtherDeductionAmount()) : "")
                                .collect(Collectors.joining("; ")) : "";

                String otherDeductionStartDates = driver.getOtherDeductions() != null ?
                        driver.getOtherDeductions().stream()
                                .map(deduction -> deduction.getOtherDeductionAmountStartDate() != null ?
                                        deduction.getOtherDeductionAmountStartDate().toString() : "")
                                .collect(Collectors.joining("; ")) : "";

                String otherDeductionEndDates = driver.getOtherDeductions() != null ?
                        driver.getOtherDeductions().stream()
                                .map(deduction -> deduction.getOtherDeductionAmountEndDate() != null ?
                                        deduction.getOtherDeductionAmountEndDate().toString() : "")
                                .collect(Collectors.joining("; ")) : "";

                String otherDeductionsEmis = driver.getOtherDeductions() != null ?
                        driver.getOtherDeductions().stream()
                                .map(deduction -> deduction.getOtherDeductionAmountEmi() != null ?
                                        String.valueOf(deduction.getOtherDeductionAmountEmi()) : "")
                                .collect(Collectors.joining("; ")) : "";


                // Orders Info (One-to-Many)
                String ordersInfo = driver.getOrders() != null ?
                        driver.getOrders().stream()
                                .map(order -> "Order ID: " + order.getId()) // Customize order fields if needed
                                .collect(Collectors.joining("; ")) : "";

                // Assets Info (One-to-Many)
                String assetsNames = driver.getAssets() != null ?
                        driver.getAssets().stream()
                                .map(asset -> asset.getItem()) // Customize asset fields if needed
                                .collect(Collectors.joining("; ")) : "";

                // Visa Info (Many-to-One)
                String visaName = driver.getVisa() != null ? driver.getVisa().getVisaName() : "";

                String[] data = {
                        String.valueOf(driver.getId()),
                        String.valueOf(driver.getAmountPending()),
                        String.valueOf(driver.getAmountReceived()),
                        String.valueOf(driver.getTotalOrders()),
                        String.valueOf(driver.getCurrentOrders()),
                        String.valueOf(driver.getJahezId()),
                        driver.getVisaExpiryDate() != null ? driver.getVisaExpiryDate().toString() : "",

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
                        otherDeductionAmounts,
                        otherDeductionStartDates,
                        otherDeductionEndDates,
                        otherDeductionsEmis,
                        ordersInfo,
                        assetsNames,
                        visaName,
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

                        driver.getPayToJahez() != null ? String.valueOf(driver.getPayToJahez()) : "0",
                        driver.getPaidByTam() != null ? String.valueOf(driver.getPaidByTam()) : "0",

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

    public ResponseEntity<ResponseStructure<Object>> countDriversWithOwnedVehicle() {
        try {
            Long count = driverDao.countOwnedVehicleDrivers();
            Long result = (count == null) ? 0L : count;

            logger.info("Successfully retrieved Owned vehicle driver count: {}", result);
            return ResponseStructure.successResponse(result, "Owned vehicle driver count retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error while fetching Owned vehicle driver count.", e);
            return ResponseStructure.errorResponse(null, 500, "Internal server error.");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> countDriversWithVehicleTypeNotOwned() {
        try {
            Long count = driverDao.countDriversWithVehicleTypeNotOwned();  // Call to the DAO method
            Long result = (count == null) ? 0L : count;  // Handle null values by defaulting to 0

            logger.info("Successfully retrieved count of drivers with vehicle type not 'Owned': {}", result);
            return ResponseStructure.successResponse(result, "Driver count with vehicle type not 'Owned' retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error while fetching driver count with vehicle type not 'Owned'.", e);
            return ResponseStructure.errorResponse(null, 500, "Internal server error.");
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

    public ResponseEntity<ResponseStructure<Page<Driver>>> rentedSRentedVeichleType(int offset, int pageSize, String field) {
        ResponseStructure<Page<Driver>> responseStructure = new ResponseStructure<>();
        try {
            logger.info("Searching for drivers with ownedVeichleType : {}");

            Page<Driver> driverList = driverDao.rentedSRented(offset, pageSize, field);
            if (driverList.isEmpty()) {
                logger.warn("No drivers found with ownedVeichleType{}");
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("Driver Not Found With ownedVeichleType ");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            } else {
                logger.info("Found {} drivers with ownedVeichleType {}", driverList);
                responseStructure.setStatus(HttpStatus.OK.value());
                responseStructure.setMessage("Driver Found With ownedVeichleType ");
                responseStructure.setData(driverList);
                return new ResponseEntity<>(responseStructure, HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error("Error searching for drivers with ownedVeichleType {}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<ResponseStructure<Object>> deleteDriver(Long driverId) {
        try {
            Driver driver = driverDao.getById(driverId);
            if (driver == null) {
                logger.warn("Driver not found with ID: {}", driverId);
                return ResponseStructure.errorResponse(null, 404, "Driver not found");
            }

            //  Delete all penalties associated with the driver
            List<Penalty> penalties = penaltyDao.findByDriverId(driver.getId());
            if (penalties != null && !penalties.isEmpty()) {
                penaltyDao.deleteAll(penalties);
                logger.info("Penalties deleted for Driver ID: {}", driverId);
            }

            /*Delete all FleetHistory associated with the driver
            List<FleetHistory> fleetHistories = fleetHistoryDao.findByDriverId(driver.getId());
            if (fleetHistories != null && !fleetHistories.isEmpty()) {
                fleetHistoryRepository.deleteAll(fleetHistories);
                logger.info("FleetHistory deleted for Driver ID: {}", driverId);}*/

            // Delete all FleetHistory associated with the driver
            fleetHistoryDao.deleteFleetHistoryByDriverId(driverId);
            logger.info("FleetHistory deleted for Driver ID: {}", driverId);


            //  Nullify driver references in related entities
            fleetDao.nullifyFleetDriver(driverId);

            //  Delete the driver
            driverDao.deleteDriver(driver);
            logger.info("Driver deleted successfully: {}", driverId);
            return ResponseStructure.successResponse(null, "Driver and associated records deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting Driver", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<List<DriverNamesReq>>> getAllDriverNames() {
        ResponseStructure<List<DriverNamesReq>> responseStructure = new ResponseStructure<>();
        try {
            List<Driver> drivers = driverDao.getAllDrivers();
            if (drivers.isEmpty()) {
                logger.warn("No Driver found.");
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("No Driver found.");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }

            List<DriverNamesReq> DriverNamesReqs = drivers.stream()
                    .map(driver -> new DriverNamesReq(driver.getId(), driver.getUsername()))
                    .collect(Collectors.toList());
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Driver Names fetched successfully.");
            responseStructure.setData(DriverNamesReqs);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching driver names : ", e);
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage(e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}