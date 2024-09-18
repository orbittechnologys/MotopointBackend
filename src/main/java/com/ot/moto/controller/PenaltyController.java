package com.ot.moto.controller;

import com.ot.moto.dao.PenaltyDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreatePenaltyReq;
import com.ot.moto.dto.request.UpdatePenaltyReq;
import com.ot.moto.entity.Penalty;
import com.ot.moto.service.PenaltyServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/penalty")
@CrossOrigin(origins = "*")
public class PenaltyController {

    @Autowired
    private PenaltyServices penaltyServices;

    @Autowired
    private PenaltyDao penaltyDao;

    @PostMapping("/save")
    public ResponseEntity<ResponseStructure<Object>> savePenalty(@RequestBody CreatePenaltyReq createPenaltyReq) {
        return penaltyServices.savePenalty(createPenaltyReq);
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseStructure<Object>> updatePenalty(@RequestBody UpdatePenaltyReq updatePenaltyReq,
                                                                   @RequestParam Long fleetId,
                                                                   @RequestParam Long driverId) {
        return penaltyServices.updatePenaltyByFleetIdAndDriverId(updatePenaltyReq, fleetId, driverId);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseStructure<Object>> deletePenaltiesByFleetIdAndDriverId(@RequestParam Long fleetId,
                                                                                         @RequestParam Long driverId) {
        return penaltyServices.deletePenaltiesByFleetIdAndDriverId(fleetId, driverId);
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<ResponseStructure<Object>> getPenaltyById(@PathVariable long id) {
        return penaltyServices.getPenaltyById(id);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseStructure<Object>> getAllPenalties() {
        return penaltyServices.getAllPenalties();
    }

    @GetMapping("/getPenaltiesByFleetId/{fleetId}")
    public ResponseEntity<ResponseStructure<Object>> getPenaltiesByFleetId(@PathVariable Long fleetId,
                                                                           @RequestParam int offset,
                                                                           @RequestParam int pageSize,
                                                                           @RequestParam String field) {
        return penaltyServices.getPenaltiesByFleetId(fleetId, offset, pageSize, field);
    }

    @GetMapping("/getPenaltiesByDriverId/{driverId}")
    public ResponseEntity<ResponseStructure<Object>> getPenaltiesByDriverId(@PathVariable Long driverId,
                                                                            @RequestParam int offset,
                                                                            @RequestParam int pageSize,
                                                                            @RequestParam String field) {
        return penaltyServices.getPenaltiesByDriverId(driverId, offset, pageSize, field);
    }
}