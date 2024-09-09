package com.ot.moto.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetReq {

    private String assetName;

    private long quantity;

    private Long driverId;
}
