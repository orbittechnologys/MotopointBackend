package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.service.OrgReportsService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/orgReports")
public class OrgReportsController {

    @Autowired
    private OrgReportsService orgReportsService;

    @PostMapping(value = "/upload/orgReports", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseStructure<Object>> uploadOrgReports(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return ResponseStructure.errorResponse(null, 400, "ERROR: No data found in the file.");
            }

            return orgReportsService.uploadOrgReports(sheet);

        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "ERROR: " + e.getMessage());
        }
    }
}
