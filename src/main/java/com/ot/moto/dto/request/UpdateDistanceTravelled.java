package com.ot.moto.dto.request;

import lombok.Data;

@Data
public class UpdateDistanceTravelled {

    private long id;

    private Long distanceTravelled;

    private Double distanceTravelledAmount;
}
