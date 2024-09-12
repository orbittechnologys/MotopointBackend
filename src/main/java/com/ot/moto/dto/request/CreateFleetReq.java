package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateFleetReq {

    private String vehicleName;

    private String vehicleNumber;

    private String vehicleType;

    private LocalDate insuranceExpiryDate;

    private String insuranceDocument;

    private String registrationCertificate;

    private LocalDate dateOfPurchase;

    public long driverId;

    private String image;
}