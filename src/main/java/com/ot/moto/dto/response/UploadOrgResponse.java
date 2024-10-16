package com.ot.moto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadOrgResponse {

    private Long noOfRowsParsed;

    private Double totalCod;

    private Long totalDrivers;

    private Double totalCredit;

    private Double totalDebit;

    private Long totalS1;

    private Long totalS2;

    private Long totalS3;

    private Long totalS4;

    private Long totalS5;

    private Double totalEarnings;

    private Double profit;

}
