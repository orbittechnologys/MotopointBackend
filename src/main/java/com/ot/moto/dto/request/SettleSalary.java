package com.ot.moto.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SettleSalary {

    private Long id;

    private Double visaCharges;

    private Double otherCharges;

    private Double bonus;

    private Double incentives;

}
