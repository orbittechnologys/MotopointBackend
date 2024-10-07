package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SettleSalV2 {
    LocalDate startDate;
    LocalDate endDate;
    Double bonus;
    Double incentive;
}
