package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateMasterReq;
import com.ot.moto.service.MasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(value = "*")
@RequestMapping("/master")
public class MasterController {

    @Autowired
    private MasterService masterService;


    @Operation(summary = "Create Master", description = "Returns Master Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Master Created"),
            @ApiResponse(responseCode = "409", description = "Master Already exists")
    })
    @PostMapping("/create")
    public ResponseEntity<ResponseStructure<Object>> createMaster(@RequestBody CreateMasterReq req) {
        return masterService.createMaster(req);
    }

    @Operation(summary = "Fetch Master by slab", description = "Returns Master Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Master fetched"),
            @ApiResponse(responseCode = "404", description = "Master not found")
    })
    @GetMapping("/slab/{slab}")
    public ResponseEntity<ResponseStructure<Object>> getMasterBySlab(@PathVariable String slab){
        return masterService.getMasterBySlab(slab);
    }


    @Operation(summary = "Get all master", description = "returns List of master Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "master data Found"),
            @ApiResponse(responseCode = "404", description = "master data Not Found")})
    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllAdmin(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(defaultValue = "id") String field) {
        return masterService.getAllByMaster(page, size, field);
    }
}
