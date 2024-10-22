package com.ot.moto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentReportDTO {
    private String username;

    private Double totalBenefit;

    private Long driverId;
}
