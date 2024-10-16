package com.ot.moto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadTamResponse {

    private Long noOfRowsParsed;

    private Long totalDrivers;

    private Double amountReceived;

}
