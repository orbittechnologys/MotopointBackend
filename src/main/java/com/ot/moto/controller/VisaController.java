package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.VisaRequest;
import com.ot.moto.dto.request.VisaUpdateReq;
import com.ot.moto.service.VisaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/visa")
public class VisaController {

    @Autowired
    private VisaService visaService;

    @Operation(summary = "Create Visa", description = "Input is Create Visa Request, returns Visa Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CREATED"),
            @ApiResponse(responseCode = "409", description = "Visa Name Already Exists")
    })
    @PostMapping("/create")
    public ResponseEntity<ResponseStructure<Object>> createVisa(@RequestBody VisaRequest request) {
        return visaService.createVisa(request);
    }

    @Operation(summary = "Update Visa", description = "Input is Update Visa Request, returns Visa Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Visa Not Found"),
            @ApiResponse(responseCode = "409", description = "Visa Name Already Exists")
    })
    @PostMapping("/update")
    public ResponseEntity<ResponseStructure<Object>> updateVisa(@RequestBody VisaUpdateReq request) {
        return visaService.updateVisa(request);
    }

    @Operation(summary = "Get All Visas", description = "Returns a list of Visa objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Visas Found"),
            @ApiResponse(responseCode = "404", description = "No Visas Found")
    })
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllVisas() {
        return visaService.findAll();
    }

    @Operation(summary = "Get Visa by ID", description = "Input is Visa ID, returns Visa Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Visa Found"),
            @ApiResponse(responseCode = "404", description = "Visa Not Found")
    })
    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getVisaById(@PathVariable Long id) {
        return visaService.findById(id);
    }

    @Operation(summary = "Get Visa by Name", description = "Input is Visa Name, returns Visa Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Visa Found"),
            @ApiResponse(responseCode = "404", description = "Visa Not Found")
    })
    @GetMapping("/getByName")
    public ResponseEntity<ResponseStructure<Object>> getVisaByName(@RequestParam String visaName) {
        return visaService.getVisaByName(visaName);
    }
}
