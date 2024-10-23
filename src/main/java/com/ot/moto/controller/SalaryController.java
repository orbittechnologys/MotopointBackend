package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.SettleSalV2;
import com.ot.moto.dto.request.SettleSalariesReq;
import com.ot.moto.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/salary")
@CrossOrigin(origins = "*")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    @Operation(summary = "Get Salary", description = "Input is salary Id, returns Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary Found"),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getSalaryById(@PathVariable Long id) {
        return salaryService.getSalaryById(id);
    }


    @Operation(summary = "Get Salary", description = "returns List of Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary Found"),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAll(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(defaultValue = "id") String field) {
        return salaryService.getAll(page, size, field);
    }


    @Operation(summary = "download salary Report ", description = "returns download button for salary report")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "salary Found"),
            @ApiResponse(responseCode = "404", description = "salary Not Found")})
    @GetMapping("/download-csv")
    public ResponseEntity<InputStreamResource> downloadSalaryReport() {
        try {
            ResponseEntity<InputStreamResource> responseEntity = salaryService.generateExcelForSalary();
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Operation(summary = "Get highest Bonus", description = "Input is None, returns highestBonus Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "highestBonus Found"),
            @ApiResponse(responseCode = "404", description = "highestBonus Not Found")})
    @GetMapping("/highestBonus")
    public ResponseEntity<ResponseStructure<Object>> HighestBonus() {
        return salaryService.HighestBonus();
    }


    @Operation(summary = "Get vehicleNumber details ", description = "Input is vehicleNumber, returns vehicle and driver Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "vehicleNumber Found"),
            @ApiResponse(responseCode = "404", description = "vehicleNumber Not Found")})
    @GetMapping("/search")
    public ResponseEntity<ResponseStructure<Object>> findByDriverUsernameContaining(@RequestParam String username) {
        return salaryService.findByDriverUsernameContaining(username);
    }


    @Operation(summary = "Get  Credited Salary", description = "returns List of Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary Found"),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @GetMapping("/salaryCredited")
    public ResponseEntity<ResponseStructure<Object>> getSumOfSettledSalaries() {
        return salaryService.getSumOfSettledSalaries();
    }


    @Operation(summary = "Get Pending Salary", description = "returns List of Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary Found"),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @GetMapping("/salaryPending")
    public ResponseEntity<ResponseStructure<Object>> getSumOfNotSettledSalaries() {
        return salaryService.getSumOfNotSettledSalaries();
    }


    @Operation(summary = "settle multiple salaries ", description = "returns List of Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary updated "),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @PostMapping("/settle-salaries")
    public ResponseEntity<ResponseStructure<Object>> settleSalaries(@RequestBody SettleSalariesReq request) {
        return salaryService.settleSalaries(request);
    }

    @Operation(summary = "Get Salary", description = "returns List of Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary Found"),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @GetMapping("/getAllSalariesBetweenDates")
    public ResponseEntity<ResponseStructure<Object>> getAllSalariesBetweenDates(@RequestParam LocalDate startDate,
                                                                                @RequestParam LocalDate endDate,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size,
                                                                                @RequestParam(defaultValue = "id") String field) {
        return salaryService.getAllSalariesBetweenDates(startDate, endDate, page, size, field);
    }

    @Operation(summary = "Get Salary of particular driver", description = "returns List of Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary Found"),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @GetMapping("/findSalariesOfParticularDriver")
    public ResponseEntity<ResponseStructure<Object>> findSalariesOfParticularDriver(@RequestParam Long driverId,
                                                                                    @RequestParam LocalDate startDate,
                                                                                    @RequestParam LocalDate endDate) {
        return salaryService.findSalariesOfParticularDriver(driverId, startDate, endDate);
    }

    @Operation(summary = "Get All Salaries", description = "returns List of Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary Found"),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @GetMapping("/findAllBetweenDates")
    public ResponseEntity<ResponseStructure<Object>> findAllBetweenDates(@RequestParam LocalDate startDate,
                                                                         @RequestParam LocalDate endDate,
                                                                         @RequestParam(defaultValue = "0") int offset,
                                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                                         @RequestParam(defaultValue = "id") String field) {
        return salaryService.findAllBetweenDates(startDate, endDate, offset, pageSize, field);
    }

    @GetMapping("/total-payable-amount")
    public ResponseEntity<ResponseStructure<Object>> getTotalPayableAmountBetweenDates(@RequestParam LocalDate startDate,
                                                                                       @RequestParam LocalDate endDate) {
        return salaryService.getTotalPayableAmountBetweenDates(startDate, endDate);
    }

    @GetMapping("/total-payable-amount-driver")
    public ResponseEntity<ResponseStructure<Object>> getTotalPayableAmountBetweenDatesForParticularDriver(@RequestParam Long driverId,
                                                                                                          @RequestParam LocalDate startDate,
                                                                                                          @RequestParam LocalDate endDate) {
        return salaryService.getTotalPayableAmountBetweenDatesForParticularDriver(driverId, startDate, endDate);
    }

    @PostMapping("/v2/settle")
    public ResponseEntity<ResponseStructure<Object>> settleSalariesV2(@RequestBody SettleSalV2 request) {
        return salaryService.settleSalariesV2(request);
    }

    @PostMapping("/v2/settleSalaryForDriver")
    public ResponseEntity<ResponseStructure<Object>> settleSalaryForDriver(@RequestParam Long driverId,
                                                                           @RequestBody SettleSalV2 request) {

        return salaryService.settleSalaryForDriver(driverId, request);
    }

    @Operation(summary = "salary report", description = "No Input, returns the Excel file for Org Reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/downloadReport")
    public ResponseEntity<InputStreamResource> generateExcelForSalaries() {
        try {
            ResponseEntity<InputStreamResource> responseEntity = salaryService.generateExcelForSalaries();
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Operation(summary = "Get Total Payable  of particular driver with date Range Mobile Application API", description = "returns List of Salary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Salary Found"),
            @ApiResponse(responseCode = "404", description = "Salary Not Found")})
    @GetMapping("/findTotalPayableOfDriver")
    public ResponseEntity<ResponseStructure<Object>> findTotalPayableAmountByDriverAndDateRange(@RequestParam Long driverId,
                                                                                                @RequestParam LocalDate startDate,
                                                                                                @RequestParam LocalDate endDate) {
        return salaryService.getTotalPayableAmountByDriverAndDateRange(driverId, startDate, endDate);
    }

    @Operation(summary = "salary csv", description = "No Input, returns the Excel file for Org Reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/download-csv-date-between-for-driver")
    public ResponseEntity<InputStreamResource> generateCsvForSalariesByDriverAndDateRange(@RequestParam Long driverId,
                                                                                          @RequestParam LocalDate startDate,
                                                                                          @RequestParam LocalDate endDate) {
        try {
            ResponseEntity<InputStreamResource> responseEntity = salaryService.generateCsvForSalariesByDriverAndDateRange(driverId, startDate, endDate);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "salary csv", description = "No Input, returns the Excel file for Org Reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/download-all-csv-date-between")
    public ResponseEntity<InputStreamResource> generateCsvForAllSalariesByDateRange(@RequestParam LocalDate startDate,
                                                                                    @RequestParam LocalDate endDate) {
        try {
            ResponseEntity<InputStreamResource> responseEntity = salaryService.generateCsvForAllSalariesByDateRange(startDate, endDate);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}