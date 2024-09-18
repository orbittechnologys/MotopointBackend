package com.ot.moto.service;

import com.ot.moto.dao.FleetDao;
import com.ot.moto.dao.FleetHistoryDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Fleet;
import com.ot.moto.entity.FleetHistory;
import com.ot.moto.repository.FleetHistoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FleetHistoryService {

    @Autowired
    private FleetHistoryRepository fleetHistoryRepository;

    @Autowired
    private FleetHistoryDao fleetHistoryDao;

    @Autowired
    private FleetDao fleetDao;

    private static final Logger logger = LoggerFactory.getLogger(FleetHistoryService.class);


    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryById(Long id) {
        try {
            FleetHistory fleetHistory = fleetHistoryDao.findById(id);
            if (fleetHistory == null) {
                logger.warn("FleetHistory not found with id: {}", id);
                return ResponseStructure.errorResponse(null, 404, "FleetHistory not found with id: " + id);
            }
            return ResponseStructure.successResponse(fleetHistory, "FleetHistory found");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by id", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching FleetHistory by id: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByFleetId(Long fleetId, int offset, int pageSize, String field) {
        try {
            Page<FleetHistory> fleetHistories = fleetHistoryDao.findByFleetId(fleetId, offset, pageSize, field);
            if (fleetHistories.isEmpty()) {
                logger.warn("No FleetHistory found for fleetId: {}", fleetId);
                return ResponseStructure.errorResponse(null, 404, "No FleetHistory found for fleetId: " + fleetId);
            }
            return ResponseStructure.successResponse(fleetHistories, "FleetHistory found for fleetId");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by fleetId", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching FleetHistory by fleetId: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByDriverId(Long driverId) {
        try {
            List<FleetHistory> fleetHistories = fleetHistoryDao.findByDriverId(driverId);
            if (fleetHistories.isEmpty()) {
                logger.warn("No FleetHistory found for driverId: {}", driverId);
                return ResponseStructure.errorResponse(null, 404, "No FleetHistory found for driverId: " + driverId);
            }
            return ResponseStructure.successResponse(fleetHistories, "FleetHistory found for driverId");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by driverId", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching FleetHistory by driverId: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByFleetIdAndDriverId(Long fleetId, Long driverId) {
        try {
            List<FleetHistory> fleetHistories = fleetHistoryDao.findByFleetIdAndDriverId(fleetId, driverId);
            if (fleetHistories.isEmpty()) {
                logger.warn("No FleetHistory found for fleetId: {} and driverId: {}", fleetId, driverId);
                return ResponseStructure.errorResponse(null, 404, "No FleetHistory found for fleetId: " + fleetId + " and driverId: " + driverId);
            }
            return ResponseStructure.successResponse(fleetHistories, "FleetHistory found for fleetId and driverId");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by fleetId and driverId", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching FleetHistory by fleetId and driverId: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getAllFleetHistory(int page, int size, String field) {
        try {
            Page<FleetHistory> fleetHistorypage = fleetHistoryDao.findAll(page, size, field);
            if (fleetHistorypage.isEmpty()) {
                logger.warn("No FleetHistory found.");
                return ResponseStructure.errorResponse(null, 404, "No FleetHistory found");
            }
            return ResponseStructure.successResponse(fleetHistorypage, "FleetHistory found");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public List<FleetHistory> findByFleetIdAndDateRange(Long fleetId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // Fetch fleet history records by fleetId and date range
            List<FleetHistory> fleetHistories = fleetHistoryDao.findByFleetIdAndDateRange(fleetId, startDate, endDate);
            if (fleetHistories.isEmpty()) {
                logger.warn("No FleetHistory found for fleetId: {} between {} and {}", fleetId, startDate, endDate);
            } else {
                logger.info("FleetHistory found for fleetId: {} between {} and {}", fleetId, startDate, endDate);
            }
            return fleetHistories;
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by fleetId and date range", e);
            throw new RuntimeException("Error fetching FleetHistory by fleetId and date range: " + e.getMessage());
        }
    }


    public ResponseEntity<InputStreamResource> downloadReport(Long fleetId) {
        try {
            // Fetch FleetHistory for the specific fleetId
            List<FleetHistory> fleetHistoryList = fleetHistoryDao.findByFleetId(fleetId);
            if (fleetHistoryList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Fetch the Fleet instance
            Fleet fleet = fleetDao.getFleetById(fleetId);
            if (fleet == null) {
                return ResponseEntity.notFound().build();
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Fleet History Data");

            // Create font for the vehicle name heading
            Font headingFont = workbook.createFont();
            headingFont.setFontHeightInPoints((short) 22); // Modern font size
            headingFont.setBold(true);
            headingFont.setFontName("Arial"); // Modern font style

            // Create cell style for the vehicle name heading
            CellStyle headingStyle = workbook.createCellStyle();
            headingStyle.setFont(headingFont);
            headingStyle.setAlignment(HorizontalAlignment.CENTER);
            headingStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Add Vehicle name as a prominent heading
            Row headingRow = sheet.createRow(0);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 29)); // Adjust range to cover all columns
            Cell headingCell = headingRow.createCell(0);
            headingCell.setCellValue("Vehicle Name: " + fleet.getVehicleName());
            headingCell.setCellStyle(headingStyle);

            // Create font for the vehicle number subheading
            Font subheadingFont = workbook.createFont();
            subheadingFont.setFontHeightInPoints((short) 16); // Modern font size
            subheadingFont.setBold(true);
            subheadingFont.setFontName("Dubai Light"); // Modern font style

            // Create cell style for the vehicle number subheading
            CellStyle subheadingStyle = workbook.createCellStyle();
            subheadingStyle.setFont(subheadingFont);
            subheadingStyle.setAlignment(HorizontalAlignment.CENTER);
            subheadingStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Add Vehicle number as a subheading
            Row subheadingRow = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 29)); // Adjust range to cover all columns
            Cell subheadingCell = subheadingRow.createCell(0);
            subheadingCell.setCellValue("Vehicle Number: " + fleet.getVehicleNumber());
            subheadingCell.setCellStyle(subheadingStyle);

            // Add a blank row
            sheet.createRow(2);

            // Create header row
            Row headerRow = sheet.createRow(3);
            String[] headers = {
                    "ID", "Profit", "Fleet Assign DateTime", "Fleet UnAssign DateTime",
                    "Driver Username", "Driver Jahez ID", "Driver CPR Number", "Driver Address", "Driver Vehicle Type", "Driver Vehicle Number",
                    "Driver DL Type", "Driver DL Expiry Date", "Driver Bank Account Name", "Driver Bank Name", "Driver Bank Account Number",
                    "Driver Bank Iban Number", "Driver Bank Branch", "Driver Bank Branch Code", "Driver Bank Swift Code",
                    "Driver Bank Account Currency", "Driver Bank Mobile Pay Number", "Driver Bank Account Type",
                    "Driver Visa Amount", "Driver Bike Rent Amount", "Driver Other Deduction Amount", "Driver Remarks",
                    "Fleet Name", "Fleet Number", "Fleet Type", "Fleet Date of Purchase"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Populate rows with data
            int rowNum = 4;
            for (FleetHistory fleetHistory : fleetHistoryList) {
                Row row = sheet.createRow(rowNum++);
                Driver driver = fleetHistory.getDriver();
                Fleet fleetHistoryFleet = fleetHistory.getFleet();

                row.createCell(0).setCellValue(fleetHistory.getId());
                row.createCell(1).setCellValue(fleetHistory.getProfit() != null ? fleetHistory.getProfit() : 0.0);
                row.createCell(2).setCellValue(fleetHistory.getFleetAssignDateTime() != null ? fleetHistory.getFleetAssignDateTime().toString() : "");
                row.createCell(3).setCellValue(fleetHistory.getFleetUnAssignDateTime() != null ? fleetHistory.getFleetUnAssignDateTime().toString() : "");

                // Driver details
                if (driver != null) {
                    row.createCell(4).setCellValue(driver.getUsername());
                    row.createCell(5).setCellValue(driver.getJahezId() != null ? driver.getJahezId() : "");
                    row.createCell(6).setCellValue(driver.getCprNumber() != null ? driver.getCprNumber() : "");
                    row.createCell(7).setCellValue(driver.getAddress() != null ? driver.getAddress() : "");
                    row.createCell(8).setCellValue(driver.getVehicleType() != null ? driver.getVehicleType() : "");
                    row.createCell(9).setCellValue(driver.getVehicleNumber() != null ? driver.getVehicleNumber() : "");
                    row.createCell(10).setCellValue(driver.getDlType() != null ? driver.getDlType() : "");
                    row.createCell(11).setCellValue(driver.getDlExpiryDate() != null ? driver.getDlExpiryDate().toString() : "");
                    row.createCell(12).setCellValue(driver.getBankAccountName() != null ? driver.getBankAccountName() : "");
                    row.createCell(13).setCellValue(driver.getBankName() != null ? driver.getBankName() : "");
                    row.createCell(14).setCellValue(driver.getBankAccountNumber() != null ? driver.getBankAccountNumber() : "");
                    row.createCell(15).setCellValue(driver.getBankIbanNumber() != null ? driver.getBankIbanNumber() : "");
                    row.createCell(16).setCellValue(driver.getBankBranch() != null ? driver.getBankBranch() : "");
                    row.createCell(17).setCellValue(driver.getBankBranchCode() != null ? driver.getBankBranchCode() : "");
                    row.createCell(18).setCellValue(driver.getBankSwiftCode() != null ? driver.getBankSwiftCode() : "");
                    row.createCell(19).setCellValue(driver.getBankAccountCurrency() != null ? driver.getBankAccountCurrency() : "");
                    row.createCell(20).setCellValue(driver.getBankMobilePayNumber() != null ? driver.getBankMobilePayNumber() : "");
                    row.createCell(21).setCellValue(driver.getBankAccountType() != null ? driver.getBankAccountType() : "");
                    row.createCell(22).setCellValue(driver.getVisaAmount() != null ? driver.getVisaAmount() : 0.0);
                    row.createCell(23).setCellValue(driver.getBikeRentAmount() != null ? driver.getBikeRentAmount() : 0.0);
                    row.createCell(24).setCellValue(driver.getOtherDeductionAmount() != null ? driver.getOtherDeductionAmount() : 0.0);
                    row.createCell(25).setCellValue(driver.getRemarks() != null ? driver.getRemarks() : "");
                } else {
                    for (int i = 4; i <= 25; i++) {
                        row.createCell(i).setCellValue("");
                    }
                }

                // Fleet details
                if (fleetHistoryFleet != null) {
                    row.createCell(26).setCellValue(fleetHistoryFleet.getVehicleName() != null ? fleetHistoryFleet.getVehicleName() : "");
                    row.createCell(27).setCellValue(fleetHistoryFleet.getVehicleNumber() != null ? fleetHistoryFleet.getVehicleNumber() : "");
                    row.createCell(28).setCellValue(fleetHistoryFleet.getVehicleType() != null ? fleetHistoryFleet.getVehicleType().toString() : "");
                    row.createCell(29).setCellValue(fleetHistoryFleet.getDateOfPurchase() != null ? fleetHistoryFleet.getDateOfPurchase().toString() : "");
                } else {
                    for (int i = 26; i <= 29; i++) {
                        row.createCell(i).setCellValue("");
                    }
                }
            }

            // Write to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // Convert ByteArrayOutputStream to ByteArrayInputStream
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            // Set HTTP headers for file download
            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fleet_history_data.xlsx");
            headers1.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers1)
                    .contentLength(outputStream.size())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (IOException e) {
            // Log the error and return an internal server error response
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
