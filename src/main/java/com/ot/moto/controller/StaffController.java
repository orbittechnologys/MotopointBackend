package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateStaffReq;
import com.ot.moto.dto.request.UpdateStaffReq;
import com.ot.moto.entity.Staff;
import com.ot.moto.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @Operation(summary = "Save Staff", description = "Input is Create Staff Request, returns Staff Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"),
            @ApiResponse(responseCode = "409", description = "Staff Already Exist")})
    @PostMapping("/create")
    public ResponseEntity<ResponseStructure<Object>> createStaff(@RequestBody CreateStaffReq req) {
        return staffService.createStaff(req);
    }

    @Operation(summary = "Get Staff", description = "Input is Staff id, returns Staff Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Fetched"),
            @ApiResponse(responseCode = "404", description = "Staff Doesn't Exist")})
    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getStaff(@PathVariable Long id) {
        return staffService.getStaff(id);
    }

    @Operation(summary = "Update Staff", description = "Input is Update staff req, returns Staff Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Staff Doesn't Exist"),
            @ApiResponse(responseCode = "409", description = "Phone/Email already exists")})
    @PostMapping("/update")
    public ResponseEntity<ResponseStructure<Object>> updateStaff(@RequestBody UpdateStaffReq req) {
        return staffService.updateStaff(req);
    }


    @Operation(summary = "Get Staffs", description = "returns List of staff Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "staff Found"),
            @ApiResponse(responseCode = "404", description = "staff Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllStaff(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(defaultValue = "id") String field) {
        return staffService.getAllStaff(page, size, field);
    }

    @Operation(summary = "delete Staff", description = "Input is staff id,returns deleted Staff Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Staff Doesn't Exist"),
            @ApiResponse(responseCode = "409", description = "Phone/Email already exists")})
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseStructure<Object>> deleteStaff(@RequestParam Long id) {
        return staffService.deleteStaff(id);
    }


    @Operation(summary = "Fetch Staff By Name ", description = "returns Staff Object By Name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Staff Not Found")})
    @GetMapping(value = "/findByUsernameContaining/{name}")
    public ResponseEntity<ResponseStructure<List<Staff>>> findByUsernameContaining(@PathVariable String name) {
        return staffService.findByUsernameContaining(name);
    }

    @Operation(summary = "csv download for staffs ", description = "returns csv file of staffs")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "csv downloaded"),
            @ApiResponse(responseCode = "404", description = "csv not downloaded")})
    @GetMapping("/download-csv")
    public ResponseEntity<InputStreamResource> generateCsvForAllStaff() {
        return staffService.generateCsvForAllStaff();
    }
}