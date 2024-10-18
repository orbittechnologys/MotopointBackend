package com.ot.moto.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ResetDriverAmountReq {

    private Long driverId;

    private List<Long> deductionIds;

    private Boolean visa;

    private Boolean bike;

}