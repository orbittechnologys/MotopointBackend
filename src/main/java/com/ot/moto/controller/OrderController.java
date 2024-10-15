package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Orders;
import com.ot.moto.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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


    @Operation(summary = "Get all orders", description = "Returns List of Fleet Objects")
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

    @GetMapping("/download-csv")
    public ResponseEntity<InputStreamResource> downloadOrdersCsv() {
        return orderService.generateCsvForOrders();
    }

    @Operation(summary = "Get the Orders of driver  ", description = "Returns List of Orders Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "driver Found"),
            @ApiResponse(responseCode = "404", description = "No drivers Found")
    })
    @GetMapping(value = "/findByNameContaining")
    public ResponseEntity<ResponseStructure<List<Orders>>> findByNameContaning(@RequestParam String letter) {
        return orderService.findByDriverNameContaining(letter);
    }

    @GetMapping(value = "/totalOrders")
    public ResponseEntity<ResponseStructure<Object>> getTotalOrdersForAllDrivers() {
        return orderService.getTotalOrdersForAllDrivers();
    }

    @Operation(summary = "Get the Orders of driver  ", description = "Returns List of Orders Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "driver Found"),
            @ApiResponse(responseCode = "404", description = "No drivers Found")
    })
    @GetMapping("/highestOrders")
    public ResponseEntity<ResponseStructure<Object>> getTopDriverWithHighestLifetimeOrders() {
        return orderService.getTopDriverWithHighestLifetimeOrders();
    }

    @GetMapping("/download-csv-of-driver")
    public ResponseEntity<InputStreamResource> generateCsvForOrdersByDriver(@RequestParam Long driverId) {
        return orderService.generateCsvForOrdersByDriver(driverId);
    }

    @GetMapping("/download-csv-between-dates")
    public ResponseEntity<InputStreamResource> generateCsvForOrdersDateBetween(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return orderService.generateCsvForOrdersDateBetween(startDate, endDate);
    }

    @GetMapping("/download-csv-of-driver-between-dates")
    public ResponseEntity<InputStreamResource> generateCsvForOrdersForParticularDriverDateBetween(@RequestParam Long driverId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return orderService.generateCsvForOrdersForParticularDriverDateBetween(driverId, startDate, endDate);
    }

    @Operation(summary = "Get all orders", description = "Returns List of Fleet Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleets Found"),
            @ApiResponse(responseCode = "404", description = "No Fleets Found")
    })
    @GetMapping("/findAllDateBetween")
    public ResponseEntity<ResponseStructure<Object>> findAllDateBetween(@RequestParam LocalDate startDate,
                                                                        @RequestParam LocalDate endDate,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(defaultValue = "id") String field) {
        return orderService.findAllDateBetween(startDate, endDate, page, size, field);
    }

    @Operation(summary = "Get all orders", description = "Returns List of Fleet Objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleets Found"),
            @ApiResponse(responseCode = "404", description = "No Fleets Found")
    })
    @GetMapping("/findAllDateBetweenParticularDriver")
    public ResponseEntity<ResponseStructure<Object>> findAllDateBetweenParticularDriver(@RequestParam Long driverId,
                                                                                        @RequestParam LocalDate startDate,
                                                                                        @RequestParam LocalDate endDate,
                                                                                        @RequestParam(defaultValue = "0") int page,
                                                                                        @RequestParam(defaultValue = "10") int size,
                                                                                        @RequestParam(defaultValue = "id") String field) {
        return orderService.findAllDateBetweenParticularDriver(driverId, startDate, endDate, page, size, field);
    }
}