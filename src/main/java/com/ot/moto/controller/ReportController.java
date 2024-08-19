package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateFleetReq;
import com.ot.moto.entity.Orders;
import com.ot.moto.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/report")
@CrossOrigin(origins = "*")
public class ReportController {


    @Autowired
    private ReportService reportService;

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

    @Operation(summary = "Jahez Report", description = "Input is Jahez Report file, returns Success/Failure Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Reports Found"),
            @ApiResponse(responseCode = "404", description = "Reports Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllReport(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "id") String field) {
        return reportService.getAllReport(page, size, field);
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


    @Operation(summary = "Get Total Amount by Payment Type", description = "Returns the total amount for each payment type (BENEFIT, TAM, CASH, OTHER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/total-by-type")
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

}