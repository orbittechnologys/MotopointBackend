package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.*;
import com.ot.moto.service.BonusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bonus")
public class BonusController {

    @Autowired
    private BonusService bonusService;


    @PostMapping("/addOrderBonus")
    public ResponseEntity<ResponseStructure<Object>> addBonusOrders(@RequestBody AddBonusOrders addBonusOrders) {
        return bonusService.addBonusOrders(addBonusOrders);
    }

    @PostMapping("/addDateBonus")
    public ResponseEntity<ResponseStructure<Object>> addBonusDate(@RequestBody AddBonusDate addBonusDate) {
        return bonusService.addBonusDate(addBonusDate);
    }

    @PutMapping("/updateOrderBonus")
    public ResponseEntity<ResponseStructure<Object>> updateBonusOrders(@RequestBody UpdateBonusOrders updateBonusOrders) {
        return bonusService.updateBonusOrders(updateBonusOrders);
    }

    @PutMapping("/updateDateBonus")
    public ResponseEntity<ResponseStructure<Object>> updateBonusDate(@RequestBody UpdateBonusDate updateBonusDate) {
        return bonusService.updateBonusDate(updateBonusDate);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAll(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(defaultValue = "id") String field) {
        return bonusService.getAll(page, size, field);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseStructure<Object>> deleteBonusById(@PathVariable Long id) {
        return bonusService.deleteBonusById(id);
    }
}

