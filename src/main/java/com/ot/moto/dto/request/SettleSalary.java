package com.ot.moto.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SettleSalary {

    private Long id;

    private Long numberOfDaysSalarySettled;

    private Double bonus;

    private Double incentives;

}
