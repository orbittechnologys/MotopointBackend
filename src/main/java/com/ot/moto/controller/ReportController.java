package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateFleetReq;
import com.ot.moto.entity.Orders;
import com.ot.moto.entity.OrgReports;
import com.ot.moto.entity.Tam;
import com.ot.moto.service.OrgReportService;
import com.ot.moto.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
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


    @Operation(summary = "Jahez Report", description = "Input is Jahez Report file, returns Success/Failure Object")
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

    @Operation(summary = "Org Report", description = "Input is Org Report file, returns Success/Failure Object")
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


    @Operation(summary = "BankStatement Report", description = "Input is BankStatement Report file, returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @PostMapping(value = "/upload/bankStatement" , consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseStructure<Object>> uploadBankStatement(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            return reportService.uploadBankStatement(sheet);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseStructure.errorResponse(null,500,"ERROR");
        }
    }


    @Operation(summary = "Get Total Amount by Payment Type", description = "Returns the total amount for each payment type (BENEFIT, TAM, CASH)")
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


    @Operation(summary = "Get Total Amount by Payment ", description = "Returns the total amount for each payment type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/current-month")
    public ResponseEntity<ResponseStructure<Object>> getSumForCurrentMonth() {
        return reportService.getSumForCurrentMonth();
    }


    @Operation(summary = "Jahez Report", description = "Input is Jahez Report file, returns Success/Failure Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Reports Found"),
            @ApiResponse(responseCode = "404", description = "Reports Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllReport(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "id") String field) {
        return reportService.getAllReport(page, size, field);
    }

    @Operation(summary = "Total amount of particular date", description = "Input is date, returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @GetMapping("/getArrearsForToday")
    public ResponseEntity<ResponseStructure<Object>> getArrearsForToday() {
        return reportService.getArrearsForToday();
    }



    @Operation(summary = "Total Amount of yesterday", description = "No Input , returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @GetMapping("/getCodAmountForYesterday")
    public ResponseEntity<ResponseStructure<Object>> getCodAmountForYesterday() {
        return reportService.getSumAmountForYesterday();
    }


    @Operation(summary = "Get All OrgReports", description = "returns List of OrgReports Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OrgReports Found"),
            @ApiResponse(responseCode = "404", description = "OrgReports Not Found")})
    @GetMapping("/getAllOrg")
    public ResponseEntity<ResponseStructure<Object>> getAllOrg(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "id") String field) {
        return orgReportService.getAllOrg(page,size,field);
    }


    @Operation(summary = "Get OrgReports by driver id", description = "returns List of OrgReports Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OrgReports Found"),
            @ApiResponse(responseCode = "404", description = "OrgReports Not Found")})
    @GetMapping("/getOrgReportsByDriverId")
    public ResponseEntity<ResponseStructure<Object>> getOrgReportsByDriverId(@RequestParam String driverId) {
        return orgReportService.getOrgByDriverID(driverId);
    }


    @Operation(summary = "Fetch driver By Name in OrgsReport ", description = "returns driver Object By Name in OrgsReport")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping(value = "/findByDriverName")
    public ResponseEntity<ResponseStructure<List<OrgReports>>> findByDriverName(@RequestParam String name) {
        return orgReportService.findByDriverName(name);
    }


    @Operation(summary = "Get Total Amount by Payment ", description = "Returns the total amount for each payment type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/currentMonthOrgReport")
    public ResponseEntity<ResponseStructure<Object>> getSumForCurrentMonthOrgReport() {
        return orgReportService.getSumForCurrentMonth();
    }


    @Operation(summary = "Total Amount of yesterday", description = "No Input , returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @GetMapping("/getCodAmountForYesterdayOrgReport")
    public ResponseEntity<ResponseStructure<Object>> getCodAmountForYesterdayOrgReport() {
        return orgReportService.getSumForYesterday();
    }


    @Operation(summary = "Download Org Reports as Excel", description = "No Input, returns the Excel file for Org Reports")
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

    @Operation(summary = "Total Amount of Month Collected By driver", description = "No Input , returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occured")
    })
    @GetMapping("/sumAmountCollectedByDriver")
    public ResponseEntity<ResponseStructure<Object>> getTopDriverWithHighestAmountForCurrentMonth() {
        return orgReportService.getTopDriverWithHighestAmountForCurrentMonth();
    }

    @Operation(summary = "BankStatement Report", description = "Input is BankStatement file, returns Success/Failure Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Reports Found"),
            @ApiResponse(responseCode = "404", description = " BankStatement Reports Not Found")})
    @GetMapping("/getAllBankStatement")
    public ResponseEntity<ResponseStructure<Object>> getAllBankStatements(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "id") String field) {
        return reportService.getAllBankstatement(page, size, field);
    }
}