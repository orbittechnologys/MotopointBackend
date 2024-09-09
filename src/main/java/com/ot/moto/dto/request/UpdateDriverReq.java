package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateDriverReq {

    private Long id;

    private String jahezId;

    private String email;

    private Long phone;

    private String password;

    private String firstName;

    private String lastName;

    private String profilePic;

    private LocalDate joiningDate;

    private LocalDate visaExpiryDate;

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
