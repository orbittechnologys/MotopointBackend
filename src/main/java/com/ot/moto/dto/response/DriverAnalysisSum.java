package com.ot.moto.dto.response;

import lombok.Data;

@Data
public class DriverAnalysisSum {
    private double codAmount;
    private double totalOrders;
    private double bonus;
    private double penalties;
    private double bike;
    private double visa;
    private double other;
    private double driverAmountPending;
}