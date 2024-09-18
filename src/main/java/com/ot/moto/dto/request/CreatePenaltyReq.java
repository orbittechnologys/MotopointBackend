package com.ot.moto.dto.request;

import lombok.Data;

@Data
public class CreatePenaltyReq {

    private long fleetId;

    private long driverId;

    private String description;

    private Double amount;
}
