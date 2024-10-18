package com.ot.moto.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ResetDriverAmountReq {

    private Long driverId;

    private List<Long> deductionIds;

    private Double visaAmount;

    private Double visaAmountEmi;

    private Double bikeRentAmount;

    private Double bikeRentAmountEmi;

}