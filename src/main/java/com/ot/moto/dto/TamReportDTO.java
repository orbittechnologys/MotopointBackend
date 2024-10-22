package com.ot.moto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TamReportDTO {

    private String username;

    private Double totalTam;

    private Long driverId;
}
