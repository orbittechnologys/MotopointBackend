package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/summary")
@CrossOrigin(value = "*")
public class SummaryController {

    @Autowired
    private SummaryService summaryService;

    @Operation(summary = "find summary by id", description = "Input is summary id, returns summary Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "found by id"),
            @ApiResponse(responseCode = "409", description = "summary id doesn't Exist")})
    @GetMapping("/findBy/{id}")
    public ResponseEntity<ResponseStructure<Object>> getSummaryById(@PathVariable Long id) {
        return summaryService.findById(id);
    }


    @Operation(summary = "find all summaries", description = "Input is None, returns all summary Objects")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "All Summaries found"),
            @ApiResponse(responseCode = "409", description = "Summaries doesn't Exist")})
    @GetMapping("/findAll")
    public ResponseEntity<ResponseStructure<Object>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String field) {
        return summaryService.findAll(page, size, field);
    }


    @Operation(summary = "download summary report", description = "Input is none, returns summary file ")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "file downloaded"),
            @ApiResponse(responseCode = "409", description = "summary file did not download ")})
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadExcelForSummary() {
        return summaryService.generateExcelForSummary();
    }
}
