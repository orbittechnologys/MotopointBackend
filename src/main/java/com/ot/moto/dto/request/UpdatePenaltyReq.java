package com.ot.moto.dto.request;

import com.ot.moto.entity.Penalty;
import lombok.Data;

@Data
public class UpdatePenaltyReq {

    private long id;

    private long fleetId;

    private long driverId;

    private String description;

    private Double amount;

    private Penalty.PenaltyStatus status;
}
