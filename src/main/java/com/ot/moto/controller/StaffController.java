package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateAdminReq;
import com.ot.moto.dto.request.CreateStaffReq;
import com.ot.moto.dto.request.UpdateStaffReq;
import com.ot.moto.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
