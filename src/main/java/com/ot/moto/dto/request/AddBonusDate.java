package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddBonusDate {

    private LocalDate specialDate;

    private Double dateBonusAmount;
}