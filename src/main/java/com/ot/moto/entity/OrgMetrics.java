package com.ot.moto.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OrgMetrics {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private long id;

    private Long noOfRowsParsed;

    private Double totalCod;

    private Long totalDrivers;

    private Double totalCredit;

    private Double totalDebit;

    private Long totalS1;

    private Long totalS2;

    private Long totalS3;

    private Long totalS4;

    private Long totalS5;

    private Double totalEarnings;

    private Double profit;

    private LocalDateTime dateTime;

    private String fileName;
}


