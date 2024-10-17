package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    @Autowired
    private MetricService metricService;


    @GetMapping("/findAllOrgMetricsByDateTimeBetween")
    public ResponseEntity<ResponseStructure<Object>> findAllOrgMetricsByDateTimeBetween(@RequestParam LocalDate startDate,
                                                                                        @RequestParam LocalDate endDate,
                                                                                        @RequestParam(defaultValue = "0") int page,
                                                                                        @RequestParam(defaultValue = "10") int size,
                                                                                        @RequestParam(defaultValue = "id") String field) {
        return metricService.findAllOrgMetricsByDateTimeBetween(startDate, endDate, page, size, field);

    }

    @GetMapping("/findAllTamMetricsByDateTimeBetween")
    public ResponseEntity<ResponseStructure<Object>> findAllTamMetricsByDateTimeBetween(@RequestParam LocalDate startDate,
                                                                                        @RequestParam LocalDate endDate,
                                                                                        @RequestParam(defaultValue = "0") int page,
                                                                                        @RequestParam(defaultValue = "10") int size,
                                                                                        @RequestParam(defaultValue = "id") String field) {
        return metricService.findAllTamMtericsDateTimeBetween(startDate, endDate, page, size, field);
    }

    @GetMapping("/findAllPaymentMetricsByDateTimeBetween")
    public ResponseEntity<ResponseStructure<Object>> findAllPaymentMetricsByDateTimeBetween(@RequestParam LocalDate startDate,
                                                                                            @RequestParam LocalDate endDate,
                                                                                            @RequestParam(defaultValue = "0") int page,
                                                                                            @RequestParam(defaultValue = "10") int size,
                                                                                            @RequestParam(defaultValue = "id") String field) {
        return metricService.findAllPaymentMetricsDateTimeBetween(startDate, endDate, page, size, field);
    }

}