package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OtherDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double otherDeductionAmount;

    private LocalDate otherDeductionAmountStartDate;

    private LocalDate otherDeductionAmountEndDate;

    private Double otherDeductionAmountEmi;

    private String otherDeductionDescription;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("otherdriver")
    private Driver driver;
}