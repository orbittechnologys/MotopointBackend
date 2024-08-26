package com.ot.moto.entity;

import com.ot.moto.service.SummaryService;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private Double visaCharges;

    private Double otherCharges;

    private Double bonus;

    private Double incentives;

    private String status;

    @ManyToOne
    @JoinColumn
    private Driver driver;

}
