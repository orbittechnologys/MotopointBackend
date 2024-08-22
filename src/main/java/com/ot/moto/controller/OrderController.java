package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.UpdateFleetReq;
import com.ot.moto.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Orders", description = "No Input, returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports Found"),
            @ApiResponse(responseCode = "404", description = "Reports Not Found")
    })
    @GetMapping("/getOrderCount")
    public ResponseEntity<ResponseStructure<Object>> getTotalOrdersForYesterday() {
        return orderService.getTotalOrdersForYesterday();
    }


    @Operation(summary = "Orders", description = "Input is Order Month and Year, returns Success/Failure Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports Found"),
            @ApiResponse(responseCode = "404", description = "Reports Not Found")
    })
    @GetMapping("/getOrderCountByMonth")
    public ResponseEntity<ResponseStructure<Object>> getTotalOrdersForMonth(@RequestParam int year, @RequestParam int month) {
        return orderService.getTotalOrdersForMonth(year, month);
    }


    @Operation(summary = "Get Fleets", description = "Returns List of Fleet Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleets Found"),
            @ApiResponse(responseCode = "404", description = "No Fleets Found")
    })
    @GetMapping("/findAll")
    public ResponseEntity<ResponseStructure<Object>> findAll(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "id") String field) {
        return orderService.findAll(page, size, field);
    }


}
