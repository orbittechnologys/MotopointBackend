package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddOtherDedution {

    private Long driverId;

    private Double otherDeductionAmount;

    private LocalDate otherDeductionAmountStartDate;

    private LocalDate otherDeductionAmountEndDate;

    private Double otherDeductionAmountEmi;

    private String otherDeductionDescription;

}