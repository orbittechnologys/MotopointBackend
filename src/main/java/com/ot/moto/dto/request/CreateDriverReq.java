package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDriverReq {

    private String jahezId;

    private String email;

    private String phone;

    private String password;

    private String firstName;

    private String lastName;

    private String profilePic;

    private LocalDate joiningDate;

    private double amountPending;

    private double amountReceived;

    private Long totalOrders;

    private LocalDate visaExpiryDate;

    private double salaryAmount;

    private String address;

    private String referenceLocation;

    private String visaType;

    private String visaProcurement;

    private String nationality;

    private String passportNumber;

    private String cprNumber;

    private String vehicleType;

    private String licenceType;

    private String licenceNumber;

    private String licenceExpiryDate;

    private String licensePhotoUrl;

    private String rcPhotoUrl;

    private String bankAccountName;

    private String bankName;

    private String bankAccountNumber;

    private String bankIbanNumber;

    private String bankBranch;
    private String bankBranchCode;

    private String bankSwiftCode;

    private String bankIfsc;

    private String bankAccountCurrency;

    private String bankMobilePayNumber;

    private String passbookImageUrl;
}
