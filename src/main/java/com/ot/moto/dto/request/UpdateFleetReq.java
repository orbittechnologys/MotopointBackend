package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateFleetReq {

    private long id;

    private String vehicleName;

    private String vehicleNumber;

    private String vehicleType;

    private LocalDate insuranceExpiryDate;

    private String insuranceDocument;

    public Long driverId;

    private String image;

    private String registrationCertificate;

    private LocalDate dateOfPurchase;
}
