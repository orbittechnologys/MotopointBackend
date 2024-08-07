package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateDriverReq;
import com.ot.moto.dto.request.UpdateDriverReq;
import com.ot.moto.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/driver")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private DriverService driverService;

    @Operation(summary = "Save Driver", description = "Input is Create driver Request, returns Driver Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"),
            @ApiResponse(responseCode = "409", description = "Staff Already Exist")})
    @PostMapping("/create")
    public ResponseEntity<ResponseStructure<Object>> createStaff(@RequestBody CreateDriverReq req) {
        return driverService.createDriver(req);
    }

    @Operation(summary = "Get Driver", description = "Input is Driver Id, returns Driver Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getDriver(@PathVariable Long id) {
        return driverService.getDriver(id);
    }

    @Operation(summary = "Update Driver", description = "Input is Update driver Request, returns Driver Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Driver Doesn't Exist"),
            @ApiResponse(responseCode = "409", description = "Phone/Email already exists")})
    @PostMapping("/update")
    public ResponseEntity<ResponseStructure<Object>> updateStaff(@RequestBody UpdateDriverReq req) {
        return driverService.updateDriver(req);
    }

    @Operation(summary = "Get Drivers", description = "returns List of Driver Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllAdmin(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(defaultValue = "id") String field) {
        return driverService.getAllDriver(page,size,field);
    }

}
