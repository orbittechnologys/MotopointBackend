package com.ot.moto.service;


import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.SummaryDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Summary;
import com.ot.moto.entity.Tam;
import com.ot.moto.repository.SummaryRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class SummaryService {

    @Autowired
    private SummaryDao summaryDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private SummaryRepository summaryRepository;


    private static final Logger logger = LoggerFactory.getLogger(SummaryService.class);

    public ResponseEntity<ResponseStructure<Object>> findById(Long id) {
        try {
            Summary summary = summaryDao.findById(id);
            if (Objects.isNull(summary)) {
                logger.warn("No Summary Report found. Invalid ID:" + id);
                return ResponseStructure.errorResponse(null, 404, "Invalid Id:" + id);
            }
            return ResponseStructure.successResponse(summary, "Summary Report found");
        } catch (Exception e) {
            logger.error("Error fetching single Summary Report ", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAll(int page, int size, String field) {
        try {
            Page<Summary> summaryPage = summaryDao.findAll(page, size, field);
            if (summaryPage.isEmpty()) {
                logger.warn("No Summary Reports found.");
                return ResponseStructure.errorResponse(null, 404, "No Summary Reports found");
            }
            return ResponseStructure.successResponse(summaryPage, "All Summary Reports found");
        } catch (Exception e) {
            logger.error("Error fetching Summary Reports", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<InputStreamResource> generateExcelForSummary() {
        try {
            List<Summary> summaryList = summaryRepository.findAll();
            if (summaryList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Summary Data");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "DriverName", "Deliveries", "Salary", "Bonus", "PayToJahez", "PaidByTam", "Profit"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Summary summary : summaryList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(summary.getId());
                row.createCell(1).setCellValue(summary.getDriverName());
                row.createCell(2).setCellValue(summary.getDeliveries() != null ? summary.getDeliveries() : 0);
                row.createCell(3).setCellValue(summary.getSalary() != null ? summary.getSalary() : 0);
                row.createCell(4).setCellValue(summary.getBonus() != null ? summary.getBonus() : 0.0);
                row.createCell(5).setCellValue(summary.getPayToJahez() != null ? summary.getPayToJahez() : 0.0);
                row.createCell(6).setCellValue(summary.getPaidByTam() != null ? summary.getPaidByTam() : 0.0);
                row.createCell(7).setCellValue(summary.getProfit() != null ? summary.getProfit() : 0.0);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=summary_data.xlsx");
            headers1.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers1)
                    .contentLength(outputStream.size())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}