package com.ot.moto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverReportDTO {
    private Long jahezId;
    private Double totalCod;
    private String username;
    private Long driverId;
}