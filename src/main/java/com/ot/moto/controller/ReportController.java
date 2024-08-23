package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.OrgReports;
import com.ot.moto.entity.Payment;
import com.ot.moto.service.OrgReportService;
import com.ot.moto.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
@CrossOrigin(origins = "*")
public class ReportController {


    @Autowired
    private ReportService reportService;

    @Autowired
    private OrgReportService orgReportService;


    @Operation(summary = "upload JahezReport", description = "Input is Jahez Report file, returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @PostMapping(value = "/upload/jahez", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseStructure<Object>> uploadJahezReport(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            return reportService.uploadJahezReport(sheet);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseStructure.errorResponse(null, 500, "ERROR");
        }
    }

    @Operation(summary = "upload OrgReport", description = "Input is Org Report file, returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @PostMapping(value = "/upload/orgReports", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseStructure<Object>> uploadOrgReports(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return ResponseStructure.errorResponse(null, 400, "ERROR: No data found in the file.");
            }

            return orgReportService.uploadOrgReports(sheet);

        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "ERROR: " + e.getMessage());
        }
    }


    @Operation(summary = "upload bankStatement", description = "Input is BankStatement Report file, returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @PostMapping(value = "/upload/bankStatement", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseStructure<Object>> uploadBankStatement(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            return reportService.uploadBankStatement(sheet);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseStructure.errorResponse(null, 500, "ERROR");
        }
    }


//    _____

    @Operation(summary = "Payment Type from PAYMENT(bank statement)", description = "Returns the total amount for each payment type (BENEFIT, TAM, CASH)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/getTotalAmountByPaymentType")
    public ResponseEntity<ResponseStructure<Object>> getTotalAmountByPaymentType() {
        try {
            Map<String, Double> totalAmounts = reportService.getTotalAmountByPaymentType();
            return ResponseStructure.successResponse(totalAmounts, "Total amounts by payment type retrieved successfully");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }


    @Operation(summary = "BENEFIT(payment table) ", description = "Returns the total amount for each payment type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/getSumForCurrentMonthForBenefit")
    public ResponseEntity<ResponseStructure<Object>> getSumForCurrentMonth() {
        return reportService.getSumForCurrentMonth();
    }


    @Operation(summary = "all Jahez Report from ORDERS", description = "Input is Jahez Report file, returns Success/Failure Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Reports Found"),
            @ApiResponse(responseCode = "404", description = "Reports Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllReport(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "id") String field) {
        return reportService.getAllReport(page, size, field);
    }

    //    __
    @Operation(summary = "Total amount of a date ORDERS(jahez)", description = "Input is date, returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @GetMapping("/getArrearsForToday")
    public ResponseEntity<ResponseStructure<Object>> getArrearsForToday() {
        return reportService.getArrearsForToday();
    }


    @Operation(summary = "BENEFIT(payment table)", description = "No Input , returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @GetMapping("/getAmountForYesterdayForBenefit")
    public ResponseEntity<ResponseStructure<Object>> getCodAmountForYesterday() {
        return reportService.getSumAmountForYesterday();
    }


    @Operation(summary = "All OrgReport ", description = "returns List of OrgReports Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OrgReports Found"),
            @ApiResponse(responseCode = "404", description = "OrgReports Not Found")})
    @GetMapping("/getAllOrg")
    public ResponseEntity<ResponseStructure<Object>> getAllOrg(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "id") String field) {
        return orgReportService.getAllOrg(page, size, field);
    }


    @Operation(summary = "OrgReport ", description = "returns List of OrgReports Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OrgReports Found"),
            @ApiResponse(responseCode = "404", description = "OrgReports Not Found")})
    @GetMapping("/getOrgReportsByDriverId")
    public ResponseEntity<ResponseStructure<Object>> getOrgReportsByDriverId(@RequestParam String driverId) {
        return orgReportService.getOrgByDriverID(driverId);
    }


    @Operation(summary = "OrgReport ", description = "returns driver Object By Name in OrgsReport")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping(value = "/findByDriverName")
    public ResponseEntity<ResponseStructure<List<OrgReports>>> findByDriverName(@RequestParam String name) {
        return orgReportService.findByDriverNameContaining(name);
    }


    @Operation(summary = "OrgReports ", description = "Returns the total amount for each payment type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/currentMonthOrgReport")
    public ResponseEntity<ResponseStructure<Object>> getSumForCurrentMonthOrgReport() {
        return orgReportService.getSumForCurrentMonth();
    }


    @Operation(summary = "OrgReports", description = "No Input , returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @GetMapping("/getCodAmountForYesterdayOrgReport")
    public ResponseEntity<ResponseStructure<Object>> getCodAmountForYesterdayOrgReport() {
        return orgReportService.getSumForYesterday();
    }


    @Operation(summary = "OrgReports", description = "No Input, returns the Excel file for Org Reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadOrgReportsExcel() {
        try {
            ResponseEntity<InputStreamResource> responseEntity = orgReportService.generateExcelForOrgReports();
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "highest COD OrgReports ", description = "No Input , returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @GetMapping("/sumAmountCollectedByDriver")
    public ResponseEntity<ResponseStructure<Object>> getTopDriverWithHighestAmountForCurrentMonth() {
        return orgReportService.getTopDriverWithHighestAmountForCurrentMonth();
    }


    @Operation(summary = "BankStatements(PAYMENT)", description = "Input is BankStatement file, returns Success/Failure Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Reports Found"),
            @ApiResponse(responseCode = "404", description = " BankStatement Reports Not Found")})
    @GetMapping("/getAllBankStatement")
    public ResponseEntity<ResponseStructure<Object>> getAllBankStatements(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "id") String field) {
        return reportService.getAllBankstatement(page, size, field);
    }

    @Operation(summary = "find driver name from payment(BENEFIT) ", description = "Input is BankStatement file, returns Success/Failure Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Reports Found"),
            @ApiResponse(responseCode = "404", description = " BankStatement Reports Not Found")})

    @GetMapping("/findPaymentsByDriverName")
    public ResponseEntity<ResponseStructure<List<Payment>>> searchPaymentsByDriverNameAndPhone(@RequestParam String name) {
        return reportService.findPaymentsByDriverUsernameContaining(name);
    }
}