package com.ot.moto.dto.request;

import lombok.Data;

@Data
public class UpdateBonusOrders {
    private Long id;

    private Long deliveryCount;

    private Double bonusAmount;
}