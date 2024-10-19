package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Settlement;
import com.ot.moto.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/settlement")
@CrossOrigin(origins = "*")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @PostMapping("/save")
    public ResponseEntity<ResponseStructure<Object>> save(@RequestParam Settlement settlement) {
        return settlementService.save(settlement);
    }

    @GetMapping("/getById")
    public ResponseEntity<ResponseStructure<Object>> getById(@RequestParam Long id) {
        return settlementService.findById(id);
    }

    @GetMapping("/findAllSettlementByDriverIdDateTimeBetween")
    public ResponseEntity<ResponseStructure<Object>> findAllSettlementByDriverIdDateTimeBetween(@RequestParam Long driverId,
                                                                                                @RequestParam LocalDate startDate,
                                                                                                @RequestParam LocalDate endDate,
                                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                                @RequestParam int size,
                                                                                                @RequestParam String field) {
        return settlementService.findAllSettlementByDriverIdDateTimeBetween(driverId, startDate, endDate, page, size, field);
    }

    @GetMapping("/findAllBySettleDateTimeBetween")
    public ResponseEntity<ResponseStructure<Object>> findAllBySettleDateTimeBetween(@RequestParam LocalDate startDate,
                                                                                    @RequestParam LocalDate endDate,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam int size,
                                                                                    @RequestParam String field) {
        return settlementService.findAllBySettleDateTimeBetween(startDate, endDate, page, size, field);
    }

    @GetMapping("/findAll")
    public ResponseEntity<ResponseStructure<Object>> findAll(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam int size,
                                                             @RequestParam String field) {
        return settlementService.findAll(page, size, field);
    }

    @GetMapping("/findAllSettlementByDriverId")
    public ResponseEntity<ResponseStructure<Object>> findAllSettlementByDriverId(@RequestParam Long driverId,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam int size,
                                                                                 @RequestParam String field) {
        return settlementService.findAllSettlementByDriverId(driverId,page, size, field);
    }
}
