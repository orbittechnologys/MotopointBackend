package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateDriverReq;
import com.ot.moto.dto.request.UpdateDriverReq;
import com.ot.moto.dto.response.DriverDetails;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.User;
import com.ot.moto.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "Get all Drivers", description = "returns List of Driver Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllDriver(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(defaultValue = "id") String field) {
        return driverService.getAllDriver(page, size, field);
    }


    @Operation(summary = "Get Driver Details", description = "Returns detailed statistics about drivers including counts for flexi visas, other visa types, riders, and drivers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver details retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/details")
    public ResponseEntity<DriverDetails> getDriverDetails() {
        DriverDetails driverDetails = driverService.getDriverDetails();
        return ResponseEntity.ok(driverDetails);
    }

    @Operation(summary = "delete Driver", description = "returns deleted Driver Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseStructure<Object>> deleteDriver(@RequestParam Long driverId) {
        return driverService.deleteDriver(driverId);
    }

    @Operation(summary = "Fetch Top Driver", description = "returns Top Driver Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping("/topDriver")
    public ResponseEntity<ResponseStructure<Object>> fetchTopDriver() {
        return driverService.fetchTopDriver();
    }


    @Operation(summary = "Fetch Driver By Name ", description = "returns Driver Object By Name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Driver Found"),
            @ApiResponse(responseCode = "404", description = "Driver Not Found")})
    @GetMapping(value = "/findByUsernameContaining/{name}")
    public ResponseEntity<ResponseStructure<List<Driver>>> findByUsernameContaining(@PathVariable String name) {
        return driverService.findByUsernameContaining(name);
    }


    @Operation(summary = "csv download for drivers ", description = "returns csv file of drivers")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "csv downloaded"),
            @ApiResponse(responseCode = "404", description = "csv not downloaded")})
    @GetMapping("/download-csv")
    public ResponseEntity<InputStreamResource> downloadDriversCsv() {
        return driverService.generateCsvForDrivers();
    }
}

