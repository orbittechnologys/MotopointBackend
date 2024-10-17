package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddMoneyReq {

    private Long driverId;

    private String mode;

    private Double amount;

    private LocalDate date;

}
