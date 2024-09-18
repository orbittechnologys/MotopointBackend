package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.service.FleetHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fleetHistory")
@CrossOrigin(origins = "*")
public class FleetHistoryController {

    @Autowired
    private FleetHistoryService fleetHistoryService;


    @Operation(summary = "Get FleetHistory by ID", description = "Returns FleetHistory Object by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FleetHistory Found"),
            @ApiResponse(responseCode = "404", description = "FleetHistory Not Found")
    })
    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryById(@PathVariable Long id) {
        return fleetHistoryService.getFleetHistoryById(id);
    }


    @Operation(summary = "Get FleetHistory by Fleet ID", description = "Returns List of FleetHistory Objects by Fleet ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FleetHistory Found"),
            @ApiResponse(responseCode = "404", description = "No FleetHistory Found for Fleet ID")
    })
    @GetMapping("/getByFleetId/{fleetId}")
    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByFleetId(@PathVariable Long fleetId,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "10") int size,
                                                                              @RequestParam(defaultValue = "id") String field) {
        return fleetHistoryService.getFleetHistoryByFleetId(fleetId,page,size,field);
    }


    @Operation(summary = "Get FleetHistory by Driver ID", description = "Returns List of FleetHistory Objects by Driver ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FleetHistory Found"),
            @ApiResponse(responseCode = "404", description = "No FleetHistory Found for Driver ID")
    })
    @GetMapping("/getByDriverId/{driverId}")
    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByDriverId(@PathVariable Long driverId) {
        return fleetHistoryService.getFleetHistoryByDriverId(driverId);
    }


    @Operation(summary = "Get FleetHistory by Fleet ID and Driver ID", description = "Returns List of FleetHistory Objects by Fleet ID and Driver ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FleetHistory Found"),
            @ApiResponse(responseCode = "404", description = "No FleetHistory Found for Fleet ID and Driver ID")
    })
    @GetMapping("/getByFleetIdAndDriverId")
    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByFleetIdAndDriverId(@RequestParam Long fleetId,
                                                                                         @RequestParam Long driverId) {
        return fleetHistoryService.getFleetHistoryByFleetIdAndDriverId(fleetId, driverId);
    }


    @Operation(summary = "Get FleetHistory", description = "Returns List of FleetHistory Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FleetHistory Found"),
            @ApiResponse(responseCode = "404", description = "No FleetHistory Found")
    })
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllFleetHistory(@RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(defaultValue = "id") String field) {
        return fleetHistoryService.getAllFleetHistory(page, size, field);
    }
}