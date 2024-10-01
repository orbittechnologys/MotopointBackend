package com.ot.moto.dto.response;

import lombok.Data;

@Data
public class DriverAnalysisSum {

    private Double codAmount;

    private Double totalOrders;

    private Double bonus;

    private Double penalties;

    private Double bike;

    private Double visa;

    private Double other;

    private Double driverAmountPending;
}