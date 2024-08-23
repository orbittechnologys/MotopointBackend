package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.AssignFleet;
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
@CrossOrigin(origins = "*")
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

    @Operation(summary = "Count Two-Wheelers", description = "Returns the count of two-wheeler vehicles in the fleet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/count/two-wheelers")
    public ResponseEntity<ResponseStructure<Object>> countTwoWheelers() {
        try {
            long count = fleetService.countTwoWheelers();
            return ResponseStructure.successResponse(count, "Count of two wheelers retrieved successfully");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    @Operation(summary = "Count Four-Wheelers", description = "Returns the count of four-wheeler vehicles in the fleet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/count/four-wheelers")
    public ResponseEntity<ResponseStructure<Object>> countFourWheelers() {
        try {
            long count = fleetService.countFourWheelers();
            return ResponseStructure.successResponse(count, "Count of four wheelers retrieved successfully");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }


    @Operation(summary = "Update Fleet", description = "Input is Update Fleet Request, returns Fleet Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleet Updated"),
            @ApiResponse(responseCode = "404", description = "Fleet Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict with existing data")
    })
    @GetMapping("/ownTypeCount")
    public ResponseEntity<ResponseStructure<Object>> getFleetCounts() {
        return fleetService.getFleetCounts();
    }


    @Operation(summary = "Assign Fleet", description = "Input is Fleet Id and Driver Id, returns Fleet Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleet Assigned"),
            @ApiResponse(responseCode = "404", description = "Fleet Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict with existing data")
    })
    @PostMapping("/assignFleet")
    public ResponseEntity<ResponseStructure<Object>> assignFleet(@RequestBody AssignFleet assignFleet) {
        return fleetService.assignFleet(assignFleet);
    }
}

