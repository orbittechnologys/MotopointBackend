package com.ot.moto.controller;

import com.ot.moto.dao.PenaltyDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreatePenaltyReq;
import com.ot.moto.dto.request.UpdatePenaltyReq;
import com.ot.moto.service.PenaltyServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResponseStructure<Object>> updatePenalty(@RequestBody UpdatePenaltyReq updatePenaltyReq) {
        return penaltyServices.updatePenaltyByFleetIdAndDriverId(updatePenaltyReq);
    }


    @DeleteMapping("/deletePenaltiesByFleetIdAndDriverId")
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

    @GetMapping("/getPenaltiesByFleetId")
    public ResponseEntity<ResponseStructure<Object>> getPenaltiesByFleetId(@RequestParam Long fleetId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @RequestParam(defaultValue = "id") String field) {
        return penaltyServices.getPenaltiesByFleetId(fleetId, page, size, field);
    }

    @GetMapping("/getPenaltiesByDriverId")
    public ResponseEntity<ResponseStructure<Object>> getPenaltiesByDriverId(@RequestParam Long driverId,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(defaultValue = "id") String field) {
        return penaltyServices.getPenaltiesByDriverId(driverId, page, size, field);
    }

    @PostMapping("/settlePenaltyByDriver")
    public ResponseEntity<ResponseStructure<Object>> settlePenaltyByDriver(@RequestParam Long fleetId,
                                                                           @RequestParam Long driverId) {
        return penaltyServices.settlePenaltyByDriver(fleetId, driverId);
    }

    @DeleteMapping("/deletePenaltiesByFleetId")
    public ResponseEntity<ResponseStructure<Object>> deletePenaltiesByFleetId(@RequestParam Long fleetId) {
        return penaltyServices.deletePenaltiesByFleetId(fleetId);
    }

    @GetMapping("/downloadPenaltyReport")
    public ResponseEntity<InputStreamResource> downloadPenaltyReport(Long fleetId) {
        try {
            ResponseEntity<InputStreamResource> responseEntity = penaltyServices.downloadPenaltyReport(fleetId);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}