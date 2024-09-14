package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.AssignFleet;
import com.ot.moto.dto.request.CreateFleetReq;
import com.ot.moto.dto.request.UpdateFleetReq;
import com.ot.moto.entity.Fleet;
import com.ot.moto.service.FleetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "Search Fleet by Vehicle Number", description = "Searches for fleets with vehicle number containing the specified substring, returns a list of Fleet objects.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleets found successfully"),
            @ApiResponse(responseCode = "404", description = "No fleets found with the specified vehicle number substring"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<ResponseStructure<List<Fleet>>> searchFleetsByVehicleNumber(
            @RequestParam String vehicleNumberSubstring) {
        return fleetService.searchFleetByVehicleNumber(vehicleNumberSubstring);
    }

    @Operation(summary = "Count Assigned Two-Wheeler Fleets", description = "Counts the number of assigned TWO_WHEELER fleets and returns the count.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No assigned TWO_WHEELER fleets found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/count-assigned-two-wheeler")
    public ResponseEntity<ResponseStructure<Long>> countAssignedTwoWheeler() {
        return fleetService.countAssignedTwoWheeler();
    }

    @Operation(summary = "Count Assigned Four-Wheeler Fleets", description = "Counts the number of assigned FOUR_WHEELER fleets and returns the count.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No assigned FOUR_WHEELER fleets found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/count-assigned-four-wheeler")
    public ResponseEntity<ResponseStructure<Long>> countAssignedFourWheeler() {
        return fleetService.countAssignedFourWheeler();
    }

    @Operation(summary = "unassignFleet Fleet", description = "Input is Fleet Id and Driver Id, returns Fleet Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleet unassigned"),
            @ApiResponse(responseCode = "404", description = "Fleet Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict with existing data")
    })
    @PostMapping("/unassignFleet")
    public ResponseEntity<ResponseStructure<Object>> unassignFleet(@RequestParam Long id) {
        return fleetService.unassignFleet(id);
    }

    @Operation(summary = "Get All AssignedFleets", description = "Returns List of Assigned Fleet Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AssignedFleets Found"),
            @ApiResponse(responseCode = "404", description = "No AssignedFleets Found")
    })
    @GetMapping("/getAllAssignedFleets")
    public ResponseEntity<ResponseStructure<Object>> getAllAssignedFleets(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "id") String field) {
        return fleetService.getAllAssignedFleets(page, size, field);
    }

    @Operation(summary = "Get All AssignedFleets", description = "Returns List of Assigned Fleet Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AssignedFleets Found"),
            @ApiResponse(responseCode = "404", description = "No AssignedFleets Found")
    })
    @GetMapping("/getAllUnAssignedFleets")
    public ResponseEntity<ResponseStructure<Object>> getAllUnAssignedFleets(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(defaultValue = "id") String field) {
        return fleetService.getAllUnAssignedFleets(page, size, field);
    }
}