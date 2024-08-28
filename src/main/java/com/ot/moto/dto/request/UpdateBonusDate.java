package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateBonusDate {
    private Long id;

    private LocalDate specialDate;

    private Double dateBonusAmount;
}