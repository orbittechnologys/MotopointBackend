package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateDriverReq {

    private Long id;

    private String jahezId;

    private String email;

    private String phone;

    private String password;

    private String firstName;

    private String lastName;

    private LocalDate joiningDate;

    private LocalDate dateOfBirth;

    private String address;

    private String referenceLocation;

    private String nationality;

    private String passportNumber;

    private LocalDate passportExpiryDate;

    private String cprNumber;

    private String vehicleType;

    private String vehicleNumber;

    private String dlType;

    private LocalDate dlExpiryDate;

    private String bankAccountName;

    private String bankName;

    private String bankAccountNumber;

    private String bankIbanNumber;

    private String bankBranch;

    private String bankBranchCode;

    private String bankSwiftCode;

    private String bankAccountCurrency;

    private String bankMobilePayNumber;

    private String bankAccountType;


    //uploads
    private String profilePic;

    private String passbookImageUrl;

    private String dlFrontPhotoUrl;
    private String dlBackPhotoUrl;

    private String rcFrontPhotoUrl;
    private String rcBackPhotoUrl;

    private String passportFrontUrl;
    private String passportBackUrl;

    private String cprFrontImageUrl;
    private String cprBackImageUrl;

    private String cprReaderImageUrl;

    private String visaCopyImageUrl;


    //visa & asset
    private List<AssetRequest> assets;

    private VisaRequest visa;
}
