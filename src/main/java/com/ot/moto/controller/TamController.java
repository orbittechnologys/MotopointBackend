package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Staff;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/tam")
@CrossOrigin(origins = "*")
public class TamController {

    @Autowired
    private TamService tamService;

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


    @GetMapping("/getByJahezRiderId")
    public ResponseEntity<ResponseStructure<Object>> getByJahezRiderId(@RequestParam Long jahezRiderId){
        return tamService.getByJahezRiderId(jahezRiderId);
    }

    @Operation(summary = "Fetch driver By Name in tam ", description = "returns driver Object By Name in tam")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping(value = "/findByDriverName")
    public ResponseEntity<ResponseStructure<List<Tam>>> findByDriverName(@RequestParam String name) {
        return tamService.findByDriverName(name);
    }


    @GetMapping("/download/tamReport")
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
}