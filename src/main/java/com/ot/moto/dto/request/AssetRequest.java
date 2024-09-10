package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetRequest {

    private String item;

    private Long quantity;

    private LocalDate localDate;
}
