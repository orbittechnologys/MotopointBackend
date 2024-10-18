package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn
    @JsonManagedReference("settlement")
    private Driver driver;

    private double totalEarnings = 0.0;

    private double totalCod = 0.0;

    private long totalS1 = 0;

    private long totalS2 = 0;

    private long totalS3 = 0;

    private long totalS4 = 0;

    private long totalS5 = 0;

    private long totalOrders = 0;

    private double totalCashCollected = 0.0;

    private double totalBenefit = 0.0;

    private double totalTam = 0.0;

    private long noOfDaysNotSettled = 0;

    private double totalVisaDeductions = 0.0;

    private double totalBikeRentDeductions = 0.0;

    private double totalOtherDeductions = 0.0;

    private double totalDeductions = 0.0;

    private double settledAmount = 0.0;

    private double totalCredit = 0.0;

    private double totalDebit = 0.0;

    @ElementCollection // Stores the list without requiring a separate entity
    private List<OdDeductions> odDeductionsList;

    @Embeddable // No separate table for OdDeductions
    @Data
    public static class OdDeductions {
        private double deductionsTotal = 0.0;

        private long noOfDays = 0;

        private double deductionsPerDay = 0.0;
    }

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime settleDateTime;

}
