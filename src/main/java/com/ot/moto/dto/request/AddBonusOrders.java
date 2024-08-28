package com.ot.moto.dto.request;

import lombok.Data;

@Data
public class AddBonusOrders {

    private Long deliveryCount;

    private Double bonusAmount;
}