package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateFleetReq;
import com.ot.moto.dto.request.UpdateFleetReq;
import com.ot.moto.service.FleetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fleet")
public class FleetController {

    @Autowired
    private FleetService fleetService;

    @Operation(summary = "Save Fleet", description = "Input is Create Fleet Request, returns Fleet Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CREATED"),
            @ApiResponse(responseCode = "409", description = "Fleet Already Exist")
    })
    @PostMapping("/create")
    public ResponseEntity<ResponseStructure<Object>> createFleet(@RequestBody CreateFleetReq req) {
        return fleetService.createFleet(req);
    }


    @Operation(summary = "Get Fleet", description = "Input is Fleet Id, returns Fleet Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleet Found"),
            @ApiResponse(responseCode = "404", description = "Fleet Not Found")
    })
    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getFleet(@PathVariable Long id) {
        return fleetService.getFleet(id);
    }


    @Operation(summary = "Get Fleets", description = "Returns List of Fleet Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleets Found"),
            @ApiResponse(responseCode = "404", description = "No Fleets Found")
    })
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllFleets(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "id") String field) {
        return fleetService.getAllFleets(page, size, field);
    }


    @Operation(summary = "Update Fleet", description = "Input is Update Fleet Request, returns Fleet Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleet Updated"),
            @ApiResponse(responseCode = "404", description = "Fleet Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict with existing data")
    })
    @PostMapping("/update")
    public ResponseEntity<ResponseStructure<Object>> updateFleet(@RequestBody UpdateFleetReq req) {
        return fleetService.updateFleet(req);
    }
}

