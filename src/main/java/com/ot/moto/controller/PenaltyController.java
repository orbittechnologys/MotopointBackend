package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreatePenaltyReq;
import com.ot.moto.dto.request.UpdatePenaltyReq;
import com.ot.moto.entity.Penalty;
import com.ot.moto.service.PenaltyServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/penalty")
public class PenaltyController {

    @Autowired
    private PenaltyServices penaltyServices;

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseStructure<Object>> deletePenaltyById(@PathVariable long id) {
        return penaltyServices.deletePenaltyById(id);
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getPenaltyById(@PathVariable long id) {
        return penaltyServices.getPenaltyById(id);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllPenalties() {
        return penaltyServices.getAllPenalties();
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseStructure<Object>> updatePenalty(@RequestBody UpdatePenaltyReq updatePenaltyReq) {
        return penaltyServices.updatePenalty(updatePenaltyReq);
    }

    @PostMapping("/save")
    public ResponseEntity<ResponseStructure<Object>> savePenalty(@RequestBody CreatePenaltyReq createPenaltyReq) {
        return penaltyServices.savePenalty(createPenaltyReq);
    }
}