package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Tam;
import com.ot.moto.service.TamService;
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

@RestController
@RequestMapping("/tam")
@CrossOrigin(origins = "*")
public class TamController {

    @Autowired
    private TamService tamService;

    @Operation(summary = "uploadTamSheet ", description = "uploads tam sheet in database")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @PostMapping(value = "/upload/tamSheet", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseStructure<Object>> uploadTamSheet(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return ResponseStructure.errorResponse(null, 400, "ERROR: No data found in the file.");
            }

            return tamService.uploadTamSheet(sheet);

        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "ERROR: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all tam", description = "Returns List of tam Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "tam Found"),
            @ApiResponse(responseCode = "404", description = "No tam Found")
    })
    @GetMapping("/findAll")
    public ResponseEntity<ResponseStructure<Object>> findAll(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "id") String field) {
        return tamService.findAll(page, size, field);
    }


    @Operation(summary = "Fetch driver By Name in tam ", description = "returns driver Object By Name in tam")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping("/getByJahezRiderId")
    public ResponseEntity<ResponseStructure<Object>> getByJahezRiderId(@RequestParam Long jahezRiderId) {
        return tamService.getByJahezRiderId(jahezRiderId);
    }

    @Operation(summary = "Fetch driver By Name in tam ", description = "returns driver Object By Name in tam")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping(value = "/findByDriverName")
    public ResponseEntity<ResponseStructure<List<Tam>>> findByDriverNameContaining(@RequestParam String name) {
        return tamService.findByDriverNameContaining(name);
    }

    @Operation(summary = "download Tam Report ", description = "returns download button for tam report")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "TamReport Found"),
            @ApiResponse(responseCode = "404", description = "TamReport Not Found")})
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadTamReport() {
        try {
            ResponseEntity<InputStreamResource> responseEntity = tamService.generateExcelForAll();
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get total sum of payInAmount for the current month", description = "Returns the total sum of payInAmount for the current month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/sumPayInAmountForCurrentMonth")
    public ResponseEntity<ResponseStructure<Object>> getSumPayInAmountForCurrentMonth() {
        try {
            Double sum = tamService.getSumPayInAmountForCurrentMonth();
            return ResponseStructure.successResponse(sum, "Total sum for current month retrieved successfully");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Error fetching sum for current month: " + e.getMessage());
        }
    }

    @Operation(summary = "Get total sum of payInAmount for yesterday", description = "Returns the total sum of payInAmount for yesterday")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS"),
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "500", description = "Failure occurred")
    })
    @GetMapping("/sumPayInAmountForYesterday")
    public ResponseEntity<ResponseStructure<Object>> getSumPayInAmountForYesterday() {
        try {
            Double sum = tamService.getSumPayInAmountForYesterday();
            return ResponseStructure.successResponse(sum, "Total sum for yesterday retrieved successfully");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Error fetching sum for yesterday: " + e.getMessage());
        }
    }
}