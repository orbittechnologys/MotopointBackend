package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateAdminReq;
import com.ot.moto.dto.request.UpdateAdminReq;
import com.ot.moto.entity.Admin;
import com.ot.moto.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Operation(summary = "Save Admin", description = "Input is Create Admin Request, returns Admin Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"),
            @ApiResponse(responseCode = "409", description = "Admin Already Exist")})
    @PostMapping("/create")
    public ResponseEntity<ResponseStructure<Object>> createAdmin(@RequestBody CreateAdminReq req) {
        return adminService.createAdmin(req);
    }

    @Operation(summary = "Get Admin", description = "Input is Create Admin Id, returns Admin Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Admin Found"),
            @ApiResponse(responseCode = "404", description = "Admin Not Found")})
    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getAdmin(@PathVariable Long id) {
        return adminService.getAdmin(id);
    }

    @Operation(summary = "Get Admins", description = "returns List of Admin Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Admin Found"),
            @ApiResponse(responseCode = "404", description = "Admin Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllAdmin(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(defaultValue = "id") String field) {
        return adminService.getAllAdmin(page,size,field);
    }

    @Operation(summary = "Update Admin", description = "Input is UpdateAdminReq, returns Admin Obj")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Admin Updated"),
            @ApiResponse(responseCode = "404", description = "Admin Not Found"),
            @ApiResponse(responseCode = "409", description = "Phone/Email already exists")})
    @PostMapping("/update")
    public ResponseEntity<ResponseStructure<Object>> updateAdmin(@RequestBody UpdateAdminReq req) {
        return adminService.updateAdmin(req);
    }

}
