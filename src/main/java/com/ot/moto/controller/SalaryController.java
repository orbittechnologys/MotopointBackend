package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/salary")
@CrossOrigin(value = "*")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    @Operation(summary = "Get Salary", description = "Input is Driver Id, returns Salary Object")
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
}
