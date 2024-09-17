package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.service.FleetHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fleetHistory")
public class FleetHistoryController {

    @Autowired
    private FleetHistoryService fleetHistoryService;


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