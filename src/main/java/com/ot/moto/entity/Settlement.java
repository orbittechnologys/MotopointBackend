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

    private double totalEarnings;

    private double totalCod;

    private long totalS1;

    private long totalS2;

    private long totalS3;

    private long totalS4;

    private long totalS5;

    private long totalOrders;

    private double totalCashCollected;

    private double totalBenefit;

    private double totalTam;

    private long noOfDaysNotSettled;

    private double totalVisaDeductions;

    private double totalBikeRentDeductions;

    private double totalDeductions;

    private double settledAmount;

    @ElementCollection // Stores the list without requiring a separate entity
    private List<OdDeductions> odDeductionsList;

    @Embeddable // No separate table for OdDeductions
    @Data
    public static class OdDeductions {
        private double deductionsTotal;

        private long noOfDays;

        private double deductionsPerDay;
    }

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime settleDateTime;

}
