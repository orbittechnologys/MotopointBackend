package com.ot.moto.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Salary {

    public enum status {
        SETTLED, NOT_SETTLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long month;

    private Long year;

    private Long noOfS1;

    private Long noOfS2;

    private Long noOfS3;

    private Long noOfS4;

    private Long noOfS5;

    private Long totalOrders;

    private Double s1Earnings;

    private Double s2Earnings;

    private Double s3Earnings;

    private Double s4Earnings;

    private Double s5Earnings;

    private Double totalEarnings; //totalEarnings = s1Earnings + ... + s5Earnings - visaCharges - otherCharges + bonus + incentives

    private Double totalDeductions;

    private Double bonus = 0.0;

    private Double incentives;

    private String status;

    private Double profit = 0.0;

    private Double emiPerDay = 0.0;

    private Double fleetPenalty = 0.0;

    private LocalDate salaryCreditDate;

    private Long numberOfDaysSalarySettled;

    private Double payableAmount = 0.0;

    @ManyToOne
    @JoinColumn
    private Driver driver;

}