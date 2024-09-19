package com.ot.moto.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateOtherDeductionReq {

    private Long id;

    private Double otherDeductionAmount;

    private LocalDate otherDeductionAmountStartDate;

    private LocalDate otherDeductionAmountEndDate;

    private String otherDeductionDescription;

}
