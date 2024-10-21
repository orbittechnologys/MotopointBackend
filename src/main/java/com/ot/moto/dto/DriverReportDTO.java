package com.ot.moto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverReportDTO {
    private Long jahezId;
    private Double totalCod;
    private String address;
    private Double amountPending;
    private Double amountReceived;
    private String bankAccountName;
    private String bankAccountNumber;
    private String nationality;
    private Double salaryAmount;
    private String username;
}