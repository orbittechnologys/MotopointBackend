package com.ot.moto.dto.request;

import lombok.Data;
import java.time.LocalDate;


@Data
public class AssetUpdateReq {

    private Long id; // Add an id field to identify the asset to be updated

    private String item;
    private Long quantity;
    private LocalDate localDate;
}
